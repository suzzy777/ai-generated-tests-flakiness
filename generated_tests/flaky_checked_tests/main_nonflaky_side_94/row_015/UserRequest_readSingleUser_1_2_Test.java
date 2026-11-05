package com.sap.scimono.client;

import com.sap.scimono.entity.User;
import com.sap.scimono.client.SCIMRequest;
import com.sap.scimono.client.SCIMResourceRequest;
import com.sap.scimono.client.SCIMResponse;
import javax.ws.rs.client.WebTarget;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static com.sap.scimono.api.API.USERS;
import static com.sap.scimono.client.query.ResourcePageQuery.indexPageQuery;
import javax.ws.rs.core.GenericType;
import com.sap.scimono.client.query.IdentityPageQuery;
import com.sap.scimono.client.query.IndexPageQuery;
import com.sap.scimono.entity.paging.PagedByIdentitySearchResult;
import com.sap.scimono.entity.paging.PagedByIndexSearchResult;
import com.sap.scimono.entity.patch.PatchBody;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserRequest_readSingleUser_1_2_Test {

    private UserRequest userRequest;

    @Mock
    private SCIMResourceRequest<User> mockResourceRequestDelegate;

    @Mock
    private SCIMRequest mockScimRequest;

    @Mock
    private WebTarget mockWebTarget;

    @Mock
    private SCIMResponse<User> mockScimResponse;

    @Mock
    private User mockUser;

    @BeforeEach
    void setUp() {
        when(mockScimResponse.get()).thenReturn(mockUser);
        userRequest = new UserRequest(mockWebTarget, mockScimRequest);
        // Use reflection to set the private field resourceRequestDelegate
        try {
            java.lang.reflect.Field field = UserRequest.class.getDeclaredField("resourceRequestDelegate");
            field.setAccessible(true);
            field.set(userRequest, mockResourceRequestDelegate);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Failed to set up test: " + e.getMessage());
        }
    }

    @Test
    void testReadSingleUser() {
        String userId = "testUserId";
        RequestDetails requestDetails = RequestDetails.DEFAULT;
        when(mockResourceRequestDelegate.readSingleResource(userId, requestDetails)).thenReturn(mockScimResponse);
        SCIMResponse<User> response = null;
        try {
            Method method = UserRequest.class.getDeclaredMethod("readSingleUser", String.class);
            method.setAccessible(true);
            response = (SCIMResponse<User>) method.invoke(userRequest, userId);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            fail("Failed to invoke method: " + e.getMessage());
        }
        assertNotNull(response);
        assertEquals(mockUser, response.get());
        verify(mockResourceRequestDelegate).readSingleResource(userId, requestDetails);
    }
}
