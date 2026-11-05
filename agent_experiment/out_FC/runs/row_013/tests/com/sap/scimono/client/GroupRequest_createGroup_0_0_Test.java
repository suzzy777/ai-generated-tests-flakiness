package com.sap.scimono.client;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.sap.scimono.entity.Group;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.ws.rs.client.WebTarget;
import java.util.LinkedHashSet;
import java.util.Set;

class GroupRequest_createGroup_0_0_Test {
    private SCIMResourceRequest<Group> resourceRequestDelegate;
    private GroupRequest groupRequest;

    @BeforeEach
    void setUp() {
        resourceRequestDelegate = mock(SCIMResourceRequest.class);
        groupRequest = new GroupRequest(mock(WebTarget.class), mock(SCIMRequest.class));
        setPrivateField(groupRequest, "resourceRequestDelegate", resourceRequestDelegate);
    }

    @Test
    void testCreateGroup() {
        Group.Builder groupBuilder = new Group.Builder("displayName");
        Group group = groupBuilder.build();
        SCIMResponse<Group> expectedResponse = mock(SCIMResponse.class);

        when(resourceRequestDelegate.createResource(group)).thenReturn(expectedResponse);

        SCIMResponse<Group> actualResponse = groupRequest.createGroup(group);

        assertEquals(expectedResponse, actualResponse);
        verify(resourceRequestDelegate).createResource(group);
    }

    private void setPrivateField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
