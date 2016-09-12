package me.itzg.titanbrowser.model;


public class ConfigurationUpdate {
    String seedHost;

    Authentication authentication;

    public static class Authentication {
        boolean authenticate;
        String authUser;
        String authPass;
    }
}
