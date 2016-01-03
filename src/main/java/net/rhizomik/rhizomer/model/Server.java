package net.rhizomik.rhizomer.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.net.URL;

/**
 * Created by http://rhizomik.net/~roberto/
 */
@Entity
public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    @Id
    private String id;
    private String endpoint;

    public Server() {}

    public Server(URL endpoint) {
        this.id = new Curie(endpoint.toString()).toString();
        this.endpoint = endpoint.toString();
    }

    public String getEndpoint() { return endpoint; }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
        this.id = new Curie(endpoint.toString()).toString();
    }

    public String getId() { return id; }
}
