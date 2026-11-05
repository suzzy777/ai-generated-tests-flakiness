package com.sap.scimono.client;

import com.sap.scimono.entity.Group;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class GroupRequest_createGroup_0_0_Test {

  @Mock
  private SCIMResourceRequest<Group> resourceRequestDelegate;

  @Mock
  private WebTarget mockWebTarget;

  @Mock
  private SCIMRequest mockScimRequest;

  private GroupRequest groupRequest;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    groupRequest = new GroupRequest(mockWebTarget, mockScimRequest);
    when(resourceRequestDelegate.createResource(any(Group.class))).thenAnswer(invocation -> {
      Group arg = invocation.getArgument(0);
      Response mockJaxRsResponse = Response.status(Response.Status.CREATED).entity(arg).build();
      return SCIMResponse.<Group>newInstance(Group.class, mockJaxRsResponse, null);
    });
  }

  @Test
  public void testCreateGroup() {
    Group group = new Group.Builder().setDisplayName("Test Group").build();

    SCIMResponse<Group> response = groupRequest.createGroup(group);

    assertNotNull(response);
    assertNotNull(response.get());
  }
}
