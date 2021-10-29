package io.github.jeyjeyemem.externalizedproperties.resolvers.database.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;

@Entity
@NamedQuery(
    name = CustomPropertiesEntity.FIND_BY_CONFIG_KEYS,
    query = "FROM CustomPropertiesEntity c WHERE c.configKey IN :configKeys"
)
public class CustomPropertiesEntity {
    public static final String FIND_BY_CONFIG_KEYS = 
        "CustomPropertiesEntity.findByConfigKeys";
    
    @Id
    private long id;
    private String configKey;
    private String config;

    public CustomPropertiesEntity() {}
    
    public CustomPropertiesEntity(long id, String configKey, String config) {
        this.id = id;
        this.configKey = configKey;
        this.config = config;
    }

    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }

    public String getConfigKey() {
        return configKey;
    }
    
    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }
}
