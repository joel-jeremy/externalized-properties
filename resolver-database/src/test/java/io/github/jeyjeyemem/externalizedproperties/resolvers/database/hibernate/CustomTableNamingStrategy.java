package io.github.jeyjeyemem.externalizedproperties.resolvers.database.hibernate;

import io.github.jeyjeyemem.externalizedproperties.resolvers.database.entities.SimpleNameValuePropertyEntity;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

/**
 * Custom hibernate naming strategy to override builtin entity table name.
 * Equivalent to using an eclipselink.session.customizer property with EclipseLink.
 */
public class CustomTableNamingStrategy extends PhysicalNamingStrategyStandardImpl {
    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment context) {
        if (!isBuiltInExternalizedPropertyTable(name)) {
            return name;
        }
        
        String customTableName = System.getProperty(
            "externalizedproperties.tableName",
            "custom_externalized_properties_table"
        );
        return Identifier.toIdentifier(customTableName);
    }

    private boolean isBuiltInExternalizedPropertyTable(Identifier name) {
        return SimpleNameValuePropertyEntity.class.getSimpleName()
            .equals(name.getText());
    }
}
