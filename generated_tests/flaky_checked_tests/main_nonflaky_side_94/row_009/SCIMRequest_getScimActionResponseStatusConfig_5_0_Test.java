package com.sap.scimono.client;

import java.lang.reflect.Field;
import java.util.EnumMap;
import java.util.Map;
import com.sap.scimono.client.SCIMRequest;
import com.sap.scimono.client.ResourceAction;
import com.sap.scimono.client.ActionResponseStatusConfig;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static com.sap.scimono.api.API.APPLICATION_JSON_SCIM;
import static com.sap.scimono.client.ResourceAction.CREATE_BULK;
import static com.sap.scimono.client.ResourceAction.CREATE_SINGLE;
import static com.sap.scimono.client.ResourceAction.DELETE;
import static com.sap.scimono.client.ResourceAction.GET_ALL;
import static com.sap.scimono.client.ResourceAction.GET_SINGLE;
import static com.sap.scimono.client.ResourceAction.PATCH_UPDATE;
import static com.sap.scimono.client.ResourceAction.PUT_UPDATE;
import static java.util.Collections.singleton;
import static javax.ws.rs.HttpMethod.PATCH;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Collections;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class SCIMRequest_getScimActionResponseStatusConfig_5_0_Test {

    private SCIMRequest scimRequest;

    private Map<ResourceAction, ActionResponseStatusConfig> scimActionsResponseStatusConfig;

    @BeforeEach
    public void setUp() throws Exception {
        scimActionsResponseStatusConfig = new EnumMap<>(ResourceAction.class);
        scimActionsResponseStatusConfig.put(ResourceAction.CREATE_SINGLE, new ActionResponseStatusConfig(Collections.singleton(Response.Status.CREATED)));
        SCIMRequest.Builder builder = invokePrivateConstructor(SCIMRequest.Builder.class);
        scimRequest = invokePrivateConstructor(SCIMRequest.class, builder);
        setPrivateField(scimRequest, "scimActionsResponseStatusConfig", scimActionsResponseStatusConfig);
    }

    @Test
    public void testGetScimActionResponseStatusConfig_existingAction() {
        ActionResponseStatusConfig config = scimRequest.getScimActionResponseStatusConfig(ResourceAction.CREATE_SINGLE);
        assertEquals(scimActionsResponseStatusConfig.get(ResourceAction.CREATE_SINGLE), config);
    }

    @Test
    public void testGetScimActionResponseStatusConfig_nonExistingAction() {
        ActionResponseStatusConfig config = scimRequest.getScimActionResponseStatusConfig(ResourceAction.DELETE);
        assertNull(config);
    }

    private <T> T invokePrivateConstructor(Class<T> clazz, Object... args) throws Exception {
        Class<?>[] parameterTypes = Arrays.stream(args).map(Object::getClass).toArray(Class<?>[]::new);
        java.lang.reflect.Constructor<T> constructor = clazz.getDeclaredConstructor(parameterTypes);
        constructor.setAccessible(true);
        return constructor.newInstance(args);
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
