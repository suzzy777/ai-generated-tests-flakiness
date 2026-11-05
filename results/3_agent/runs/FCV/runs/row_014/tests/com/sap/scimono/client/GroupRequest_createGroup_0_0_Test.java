package com.sap.scimono.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sap.scimono.entity.Group;

class GroupRequest_createGroup_0_0_Test {

    @Mock
    private SCIMResourceRequest<Group> resourceRequestDelegate;

    private GroupRequest groupRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        groupRequest = new GroupRequest(null, null); // Use default constructor
        setPrivateField(groupRequest, "resourceRequestDelegate", resourceRequestDelegate);
    }

    @Test
    void testCreateGroup() {
        Group group = mock(Group.class);
        SCIMResponse<Group> expectedResponse = mock(SCIMResponse.class);
        when(resourceRequestDelegate.createResource(any(Group.class))).thenReturn(expectedResponse);

        SCIMResponse<Group> actualResponse = groupRequest.createGroup(group);

        assertEquals(expectedResponse, actualResponse);
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
