package com.sequenceiq.periscope.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import com.sequenceiq.periscope.jpa.StringIdGenerator;

@Entity
public class Ambari {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "stringGenerator")
    @GenericGenerator(name = "stringGenerator", strategy = "com.sequenceiq.periscope.jpa.StringIdGenerator",
            parameters = @Parameter(name = StringIdGenerator.BASE_NAME, value = "ambari"))
    private String id;
    private String host;
    private String port;
    private String user;
    private String pass;

    public Ambari() {
    }

    public Ambari(String host, String port, String user, String pass) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.pass = pass;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }
}
