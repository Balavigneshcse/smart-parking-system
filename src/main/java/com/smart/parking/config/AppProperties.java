package com.smart.parking.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private final Jwt jwt = new Jwt();
    private final Mail mail = new Mail();
    private final SuperAdmin superAdmin = new SuperAdmin();
    private final Razorpay razorpay = new Razorpay();

    public Jwt getJwt() { return jwt; }
    public Mail getMail() { return mail; }
    public SuperAdmin getSuperAdmin() { return superAdmin; }
    public Razorpay getRazorpay() { return razorpay; }

    public static class Jwt {
        private String secret;
        private long expirationMs = 86400000L;
        public String getSecret() { return secret; }
        public void setSecret(String s) { this.secret = s; }
        public long getExpirationMs() { return expirationMs; }
        public void setExpirationMs(long v) { this.expirationMs = v; }
    }

    public static class Mail {
        private boolean enabled = false;
        private String from = "noreply@smartparking.com";
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean v) { this.enabled = v; }
        public String getFrom() { return from; }
        public void setFrom(String v) { this.from = v; }
    }

    public static class SuperAdmin {
        private String email, password, name, phone;
        public String getEmail() { return email; }
        public void setEmail(String v) { this.email = v; }
        public String getPassword() { return password; }
        public void setPassword(String v) { this.password = v; }
        public String getName() { return name; }
        public void setName(String v) { this.name = v; }
        public String getPhone() { return phone; }
        public void setPhone(String v) { this.phone = v; }
    }

    public static class Razorpay {
        private String keyId = "rzp_test_YOUR_KEY_ID";
        private String keySecret = "YOUR_KEY_SECRET";
        private boolean enabled = false;
        private String currency = "INR";
        public String getKeyId() { return keyId; }
        public void setKeyId(String v) { this.keyId = v; }
        public String getKeySecret() { return keySecret; }
        public void setKeySecret(String v) { this.keySecret = v; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean v) { this.enabled = v; }
        public String getCurrency() { return currency; }
        public void setCurrency(String v) { this.currency = v; }
    }
}
