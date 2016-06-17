package me.previewcode.backend.DTO;

import java.util.Date;

/**
 * The data of a comment that is send to the client
 *
 */
public class PRresponseComment extends PRComment {

    /**
     * The date in which the comment is created
     */
    public Date created_at;
    /**
     * THe user that created the comment
     */
    public User user;

}
