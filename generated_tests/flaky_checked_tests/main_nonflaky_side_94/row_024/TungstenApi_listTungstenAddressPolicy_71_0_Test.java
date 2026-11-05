package org.apache.cloudstack.network.tungsten.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import java.util.Collections;
import java.util.List;
import net.juniper.tungsten.api.ApiConnector;
import net.juniper.tungsten.api.ApiObjectBase;
import net.juniper.tungsten.api.types.Project;
import net.juniper.tungsten.api.types.NetworkPolicy;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.*;
import org.junit.After;
import org.junit.Assert;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import com.cloud.utils.Pair;
import com.cloud.utils.TungstenUtils;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.net.NetUtils;
import net.juniper.tungsten.api.ApiPropertyBase;
import net.juniper.tungsten.api.ObjectReference;
import net.juniper.tungsten.api.Status;
import net.juniper.tungsten.api.types.ActionListType;
import net.juniper.tungsten.api.types.AddressGroup;
import net.juniper.tungsten.api.types.AddressType;
import net.juniper.tungsten.api.types.ApplicationPolicySet;
import net.juniper.tungsten.api.types.ConfigRoot;
import net.juniper.tungsten.api.types.DhcpOptionType;
import net.juniper.tungsten.api.types.DhcpOptionsListType;
import net.juniper.tungsten.api.types.Domain;
import net.juniper.tungsten.api.types.FatFlowProtocols;
import net.juniper.tungsten.api.types.FirewallPolicy;
import net.juniper.tungsten.api.types.FirewallRule;
import net.juniper.tungsten.api.types.FirewallRuleEndpointType;
import net.juniper.tungsten.api.types.FirewallRuleMatchTagsType;
import net.juniper.tungsten.api.types.FirewallSequence;
import net.juniper.tungsten.api.types.FirewallServiceGroupType;
import net.juniper.tungsten.api.types.FirewallServiceType;
import net.juniper.tungsten.api.types.FloatingIp;
import net.juniper.tungsten.api.types.FloatingIpPool;
import net.juniper.tungsten.api.types.GlobalSystemConfig;
import net.juniper.tungsten.api.types.GlobalVrouterConfig;
import net.juniper.tungsten.api.types.InstanceIp;
import net.juniper.tungsten.api.types.IpamSubnetType;
import net.juniper.tungsten.api.types.KeyValuePairs;
import net.juniper.tungsten.api.types.Loadbalancer;
import net.juniper.tungsten.api.types.LoadbalancerHealthmonitor;
import net.juniper.tungsten.api.types.LoadbalancerHealthmonitorType;
import net.juniper.tungsten.api.types.LoadbalancerListener;
import net.juniper.tungsten.api.types.LoadbalancerListenerType;
import net.juniper.tungsten.api.types.LoadbalancerMember;
import net.juniper.tungsten.api.types.LoadbalancerMemberType;
import net.juniper.tungsten.api.types.LoadbalancerPool;
import net.juniper.tungsten.api.types.LoadbalancerPoolType;
import net.juniper.tungsten.api.types.LoadbalancerType;
import net.juniper.tungsten.api.types.LogicalRouter;
import net.juniper.tungsten.api.types.MacAddressesType;
import net.juniper.tungsten.api.types.NetworkIpam;
import net.juniper.tungsten.api.types.PolicyEntriesType;
import net.juniper.tungsten.api.types.PolicyManagement;
import net.juniper.tungsten.api.types.PolicyRuleType;
import net.juniper.tungsten.api.types.PortMap;
import net.juniper.tungsten.api.types.PortMappings;
import net.juniper.tungsten.api.types.PortType;
import net.juniper.tungsten.api.types.SecurityGroup;
import net.juniper.tungsten.api.types.SequenceType;
import net.juniper.tungsten.api.types.ServiceGroup;
import net.juniper.tungsten.api.types.SubnetListType;
import net.juniper.tungsten.api.types.SubnetType;
import net.juniper.tungsten.api.types.Tag;
import net.juniper.tungsten.api.types.TagType;
import net.juniper.tungsten.api.types.VirtualMachine;
import net.juniper.tungsten.api.types.VirtualMachineInterface;
import net.juniper.tungsten.api.types.VirtualNetwork;
import net.juniper.tungsten.api.types.VirtualNetworkPolicyType;
import net.juniper.tungsten.api.types.VnSubnetsType;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.network.tungsten.model.TungstenLoadBalancerMember;
import org.apache.cloudstack.network.tungsten.model.TungstenRule;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@RunWith(MockitoJUnitRunner.class)
public class TungstenApi_listTungstenAddressPolicy_71_0_Test {

    @Mock
    private ApiConnector apiConnector;

    @InjectMocks
    private TungstenApi tungstenApi;

    @Before
    public void setUp() {
        tungstenApi.setApiConnector(apiConnector);
    }

    @Test
    public void testListTungstenAddressPolicy() throws Exception {
        String projectUuid = "project-uuid";
        String policyName = "policy-name";
        Project mockProject = Mockito.mock(Project.class);
        when(mockProject.getQualifiedName()).thenReturn(Collections.singletonList("qualified-name"));
        when(tungstenApi.getTungstenObject(Project.class, projectUuid)).thenReturn(mockProject);
        NetworkPolicy mockNetworkPolicy = Mockito.mock(NetworkPolicy.class);
        when(tungstenApi.getTungstenObjectByName(NetworkPolicy.class, mockProject.getQualifiedName(), policyName)).thenReturn(mockNetworkPolicy);
        List<ApiObjectBase> result = tungstenApi.listTungstenAddressPolicy(projectUuid, policyName);
        assertEquals(1, result.size());
        assertEquals(mockNetworkPolicy, result.get(0));
    }
}
