package org.xide.models;

import jakarta.websocket.Session;

public class User {
    private Long id;
    private String name;
    private String password;
    private String email;
    private Session session;
    
    public User(String name, String password, String email, Session session) {
        this.name = name;
        this.password = password;
        this.email = email;
        this.session = session;
    }
    public User() {
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public Session getSession() {
        return session;
    }

    public static String getTableCreate(){
        return 
            """
                CREATE TABLE IF NOT EXISTS users (
                    id INT NOT NULL AUTO_INCREMENT,
                    name VARCHAR(100) NOT NULL,
                    password VARCHAR(100) NOT NULL,
                    email VARCHAR(200) NOT NULL,
                    PRIMARY KEY(id)               
                );
           """;
    }
}
