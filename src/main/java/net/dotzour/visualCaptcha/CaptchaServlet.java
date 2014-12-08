package net.dotzour.visualCaptcha;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.google.common.net.MediaType;
import com.google.common.reflect.TypeToken;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Collections.shuffle;

public class CaptchaServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(CaptchaServlet.class);
    private static final int DEFAULT_NUM_OPTIONS = 5;

    public static final String INIT_PARAM_IMAGE_ASSET_PATH = "image-asset-path";
    public static final String INIT_PARAM_AUDIO_ASSET_PATH = "audio-asset-path";

    private String imagesPath;
    private List<CaptchaAnswer> images;

    private String audioPath;
    private List<CaptchaAnswer> audios;

    private Random rand = new Random();

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        this.imagesPath = config.getInitParameter(INIT_PARAM_IMAGE_ASSET_PATH);
        if(!this.imagesPath.endsWith("/")){
            this.imagesPath = this.imagesPath + "/";
        }
        this.audioPath = config.getInitParameter(INIT_PARAM_AUDIO_ASSET_PATH);
        if(!this.audioPath.endsWith("/")){
            this.audioPath = this.audioPath + "/";
        }

        try {
            loadImages();
            loadAudios();
        } catch(RuntimeException e){
            throw new ServletException("Unable to initialize CaptchaServlet.  Failed to load resources.", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String mode = null;
        String param = null;

        String[] pathElements = req.getRequestURI().split("/");
        if(pathElements[pathElements.length - 1].equals("audio")){
            mode = "audio";
        }
        else if(pathElements.length > 1){
            mode = pathElements[pathElements.length - 2];
            param = pathElements[pathElements.length - 1];
        }

        switch(mode){
            case "start":
                doStart(req, resp, param);
                break;
            case "image":
                doImage(req, resp, param);
                break;
            case "audio":
                doAudio(req, resp, param);
                break;
            default:
                log.warn("Invalid captcha request received: {}", req.getRequestURI());
                resp.sendError(400);
                break;
        }
    }

    private void doStart(HttpServletRequest req, HttpServletResponse resp, String param) throws IOException {
        int optionCount = DEFAULT_NUM_OPTIONS;
        try{
            optionCount = Integer.parseInt(param);
        }
        catch(NumberFormatException e){
            log.warn("Invalid param value for number of options to display: '{}'.  Will use default value {}.", param, DEFAULT_NUM_OPTIONS);
        }

        String salt = UUID.randomUUID().toString();
        List<CaptchaAnswer> choices = getRandomCaptchaOptions(optionCount, salt);
        CaptchaAnswer validChoice = choices.get(rand.nextInt(optionCount));
        CaptchaAnswer audioOption = getRandomCaptchaAudio(salt);
        String fieldName = hash(UUID.randomUUID().toString(), salt);
        String audioFieldName = hash(UUID.randomUUID().toString(), salt);
        setSessionInfo(req, new CaptchaSessionInfo(fieldName, validChoice.getObfuscatedName(), audioFieldName, audioOption, choices));

        List<String> frontEndOptions = new ArrayList<>(choices.size());
        for(CaptchaAnswer choice : choices){
            frontEndOptions.add(choice.getObfuscatedName());
        }
        CaptchaFrontEndData frontendData = new CaptchaFrontEndData(
            validChoice.getValue(), fieldName, frontEndOptions, audioFieldName);

        resp.setContentType("application/json");
        resp.getWriter().write(new GsonBuilder().create().toJson(frontendData));
    }

    private void doImage(HttpServletRequest req, HttpServletResponse resp, String param) throws IOException {
        CaptchaSessionInfo sessionInfo = getSessionInfo(req);
        if(sessionInfo == null){
            resp.sendError(400);
            return;
        }

        try{
            int index = Integer.parseInt(param);
            boolean retina = "1".equals(req.getParameter("retina"));
            List<CaptchaAnswer> answers = sessionInfo.getChoices();
            if(answers != null && answers.size() > index){
                resp.setContentType("image/png");
                writeImageResponse(answers.get(index), retina, resp);
                return;
            }
            else{
                log.warn("Requested image for invalid index: {}", index);
            }
        }
        catch(NumberFormatException e){
            log.warn("Invalid image index specified in request: '{}'", param);
        }
        resp.sendError(400);
    }

    private void doAudio(HttpServletRequest req, HttpServletResponse resp, String param) throws IOException {
        CaptchaSessionInfo sessionInfo = getSessionInfo(req);
        if(sessionInfo == null){
            resp.sendError(400);
            return;
        }

        String fileType = "mp3";
        MediaType contentType = MediaType.MPEG_AUDIO;

        if(param != null && "ogg".equals(param)){
            fileType = "ogg";
            contentType = MediaType.OGG_AUDIO;
        }

        resp.setContentType(contentType.toString());
        ByteStreams.copy(getServletContext().getResourceAsStream(getAudioPath(sessionInfo.getAudioAnswer(), fileType)), resp.getOutputStream());
        resp.getOutputStream().flush();
    }

    private void writeImageResponse(CaptchaAnswer answer, boolean retina, HttpServletResponse resp) throws IOException {
        ByteStreams.copy(getServletContext().getResourceAsStream(getImagePath(answer, retina)), resp.getOutputStream());
    }

    private String hash(String somethingToHash, String salt){
        return Hashing.md5().hashString((somethingToHash + salt), Charsets.UTF_8).toString();
    }

    private void loadImages() {
        Reader reader = null;
        try{
            reader = new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream("net/dotzour/visualCaptcha/images.json"));
            images = new GsonBuilder().create().fromJson(reader, new TypeToken<ArrayList<CaptchaAnswer>>(){}.getType());
        }
        finally{
            Closeables.closeQuietly(reader);
        }
    }

    private void loadAudios() {
        Reader reader = null;
        try{
            reader = new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream("net/dotzour/visualCaptcha/audios.json"));
            audios = new GsonBuilder().create().fromJson(reader, new TypeToken<ArrayList<CaptchaAnswer>>(){}.getType());
        }
        finally{
            Closeables.closeQuietly(reader);
        }
    }

    private List<CaptchaAnswer> getRandomCaptchaOptions(int numberOfChoices, String salt) {
        List<CaptchaAnswer> options = new ArrayList<>(images);
        shuffle(options);
        List<CaptchaAnswer> choices = new ArrayList<>(numberOfChoices);
        for(CaptchaAnswer answer : options.subList(0, numberOfChoices)){
            choices.add(new CaptchaAnswer(answer.getValue(), hash(answer.getValue(), salt), answer.getPath()));
        }
        shuffle(choices);
        return choices;
    }

    private CaptchaAnswer getRandomCaptchaAudio(String salt) {
        List<CaptchaAnswer> options = new ArrayList<>(audios);
        shuffle(audios);
        return audios.get(rand.nextInt(audios.size()));
    }

    private String getImagePath(CaptchaAnswer answer, boolean retina) {
        String fileName = retina ? answer.getPath().replace(".png", "@2x.png") : answer.getPath();
        return imagesPath + fileName;
    }

    private String getAudioPath(CaptchaAnswer answer, String fileType){
        String path = answer.getPath();
        if(fileType.equals("ogg")){
            path = path.replace(".mp3", ".ogg");
        }
        return audioPath + path;
    }

    private CaptchaSessionInfo getSessionInfo(HttpServletRequest req){
        return (CaptchaSessionInfo) req.getSession(true).getAttribute(CaptchaSessionInfo.class.getName());
    }

    private void setSessionInfo(HttpServletRequest req, CaptchaSessionInfo sessionInfo){
        req.getSession(true).setAttribute(CaptchaSessionInfo.class.getName(), sessionInfo);
    }
}