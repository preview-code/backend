package me.previewcode.backend.DTO;

import java.net.URL;

/**
 * The data of a user
 * 
 * @author PReview-Code
 *
 */
public class User {
    /**
     * The login name of the user
     */
    public String login;
    /**
     * The url to the users homepage
     */
    public URL html_url;
    /**
     * The url to the users avatar
     */
    public String avatar_url;

}
