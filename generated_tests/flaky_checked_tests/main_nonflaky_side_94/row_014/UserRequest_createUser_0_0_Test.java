package com.sap.scimono.client;

import javax.ws.rs.client.WebTarget;
import com.sap.scimono.client.SCIMRequest;
import com.sap.scimono.client.SCIMResourceRequest;
import com.sap.scimono.client.SCIMResponse;
import com.sap.scimono.entity.User;
import java.lang.reflect.Field;
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
public class UserRequest_createUser_0_0_Test {

    @Mock
    private WebTarget mockWebTarget;

    @Mock
    private SCIMRequest mockScimRequest;

    @Mock
    private SCIMResourceRequest<User> mockResourceRequestDelegate;

    private UserRequest userRequest;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        userRequest = new UserRequest(mockWebTarget, mockScimRequest);
        // Use reflection to set the private field
        Field resourceRequestDelegateField = UserRequest.class.getDeclaredField("resourceRequestDelegate");
        resourceRequestDelegateField.setAccessible(true);
        resourceRequestDelegateField.set(userRequest, mockResourceRequestDelegate);
    }

    @Test
    public void testCreateUser() {
        User.Builder userBuilder = new User.Builder("userName");
        User testUser = userBuilder.build();
        SCIMResponse<User> mockResponse = mock(SCIMResponse.class);
        when(mockResourceRequestDelegate.createResource(testUser)).thenReturn(mockResponse);
        SCIMResponse<User> response = userRequest.createUser(testUser);
        assertNotNull(response);
        verify(mockResourceRequestDelegate, times(1)).createResource(testUser);
    }
}
