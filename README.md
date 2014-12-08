visualCaptcha-java
==================

Plain Java Servlet backend for visualCaptcha


Configuration
-------------

1. Add dependency on visualCaptcha-java to your project.

2. Copy the directories *assets/audios* and *assets/images* into your web root.

3. Register and configure the servlet in your web.xml:


    <servlet>
        <servlet-name>Captcha</servlet-name>
        <servlet-class>net.dotzour.visualCaptcha.CaptchaServlet</servlet-class>
        <init-param>
            <param-name>image-asset-path</param-name>
            <param-value>images/visualCaptcha</param-value>
        </init-param>
        <init-param>
            <param-name>audio-asset-path</param-name>
            <param-value>audio</param-value>
        </init-param>
    </servlet>
    
    <servlet-mapping>
        <servlet-name>Captcha</servlet-name>
        <url-pattern>/captcha/*</url-pattern>
    </servlet-mapping>

4. Configure your front-end to communicate with the servlet path you choose.