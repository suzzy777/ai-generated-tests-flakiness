package com.sap.scimono.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sap.scimono.entity.Group;
import com.sap.scimono.entity.Meta;
import com.sap.scimono.entity.Group.Builder;
import java.util.LinkedHashSet;
import java.util.Set;

public class GroupRequest_createGroup_0_0_Test {

    @Mock
    private SCIMResourceRequest<Group> resourceRequestDelegateMock;

    private GroupRequest groupRequest;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        groupRequest = new GroupRequest(mock(javax.ws.rs.client.WebTarget.class), mock(SCIMRequest.class));
        // Using reflection to set the private field
        try {
            java.lang.reflect.Field field = GroupRequest.class.getDeclaredField("resourceRequestDelegate");
            field.setAccessible(true);
            field.set(groupRequest, resourceRequestDelegateMock);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testCreateGroup() {
        Builder groupBuilder = new Builder("id").setDisplayName("displayName").setExternalId("externalId");
        Group group = groupBuilder.build();
        SCIMResponse<Group> expectedResponse = mock(SCIMResponse.class);

        when(resourceRequestDelegateMock.createResource(group)).thenReturn(expectedResponse);

        SCIMResponse<Group> actualResponse = groupRequest.createGroup(group);

        assertEquals(expectedResponse, actualResponse);
    }
}
