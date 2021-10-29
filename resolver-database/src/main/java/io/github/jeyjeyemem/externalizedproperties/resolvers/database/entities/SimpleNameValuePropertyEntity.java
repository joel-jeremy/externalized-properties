package io.github.jeyjeyemem.externalizedproperties.resolvers.database.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * A simple JPA entity which has name, value, and description database columns.
 */
@Entity
public class SimpleNameValuePropertyEntity {
    @Id
    private String name;
    private String value;
    private String description;

    /**
     * Default constructor.
     */
    public SimpleNameValuePropertyEntity() {
        this("", "", "");
    }

    /**
     * Constructor.
     * 
     * @param name The name of the property.
     * @param value The value of the property.
     */
    public SimpleNameValuePropertyEntity(String name, String value) {
        this(name, value, "");
    }

    /**
     * Constructor.
     * 
     * @param name The name of the property.
     * @param value The value of the property.
     * @param description The description of the property.
     */
    public SimpleNameValuePropertyEntity(String name, String value, String description) {
        this.name = name;
        this.value = value;
        this.description = description;
    }

    /**
     * Get the name of the property.
     * 
     * @return The name of the property.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of the property.
     * 
     * @param name The name of the property.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the value of the property.
     * 
     * @return The value of the property.
     */
    public String getValue() {
        return value;
    }

    /**
     * Set the value of the property.
     * 
     * @param value The value of the property.
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Get the description of the property.
     * 
     * @return The description of the property.
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Set the description of the property.
     * 
     * @param description The description of the property.
     */
    public void setDescription(String description) {
        this.description = description;
    }
}
