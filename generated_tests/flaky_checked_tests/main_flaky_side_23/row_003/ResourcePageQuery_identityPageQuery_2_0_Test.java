package com.sap.scimono.client.query;

import com.sap.scimono.client.query.IdentityPageQuery;
import com.sap.scimono.client.query.ResourcePageQuery;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.HashMap;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import javax.ws.rs.client.WebTarget;

public class ResourcePageQuery_identityPageQuery_2_0_Test {

    @Test
    public void testIdentityPageQuery() throws NoSuchFieldException, IllegalAccessException {
        // Invoke the identityPageQuery method
        IdentityPageQuery identityPageQuery = ResourcePageQuery.identityPageQuery();
        // Assert that the returned object is not null
        assertNotNull(identityPageQuery);
        // Use reflection to access the private field 'resourcePageQuery' in IdentityPageQuery
        Field resourcePageQueryField = IdentityPageQuery.class.getDeclaredField("resourcePageQuery");
        resourcePageQueryField.setAccessible(true);
        ResourcePageQuery resourcePageQuery = (ResourcePageQuery) resourcePageQueryField.get(identityPageQuery);
        // Use reflection to access the private field 'queryParams' in ResourcePageQuery
        Field queryParamsField = ResourcePageQuery.class.getDeclaredField("queryParams");
        queryParamsField.setAccessible(true);
        Map<String, Object> queryParams = (Map<String, Object>) queryParamsField.get(resourcePageQuery);
        // Use reflection to access the private field 'DEFAULT_START_ID' in ResourcePageQuery
        Field defaultStartIdField = ResourcePageQuery.class.getDeclaredField("DEFAULT_START_ID");
        defaultStartIdField.setAccessible(true);
        String defaultStartId = (String) defaultStartIdField.get(null);
        // Use reflection to access the private field 'DEFAULT_COUNT' in ResourcePageQuery
        Field defaultCountField = ResourcePageQuery.class.getDeclaredField("DEFAULT_COUNT");
        defaultCountField.setAccessible(true);
        int defaultCount = (int) defaultCountField.get(null);
        // Assert that the queryParams map contains the correct START_ID_PARAM and COUNT_PARAM values
        assertEquals(defaultStartId, queryParams.get("startId"));
        assertEquals(defaultCount, queryParams.get("count"));
    }
}
