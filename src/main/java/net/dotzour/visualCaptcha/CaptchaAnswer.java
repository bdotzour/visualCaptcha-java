package net.dotzour.visualCaptcha;

import java.io.Serializable;

public class CaptchaAnswer implements Serializable {

   private static final long serialVersionUID = -1747574829879570623L;

   private String value;
   private String path;
   private String obfuscatedName;

   public CaptchaAnswer( String value, String obfuscatedName, String path ) {
      this.value = value;
      this.obfuscatedName = obfuscatedName;
      this.path = path;
   }

   public String getValue() {
      return value;
   }

   public String getObfuscatedName() {
      return obfuscatedName;
   }

   public String getPath() {
      return path;
   }
}
