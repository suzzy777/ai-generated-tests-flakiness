package org.apache.hadoop.yarn.service.client;

import org.apache.hadoop.yarn.exceptions.YarnException;
import org.apache.hadoop.yarn.service.api.records.Service;
import org.apache.hadoop.yarn.service.api.records.ServiceState;
import org.apache.hadoop.yarn.service.client.ServiceClient;
import org.junit.Before;
import org.junit.Test;
import java.io.File;
import java.io.IOException;
import org.mockito.*;
import org.junit.After;
import org.junit.Assert;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.shaded.com.google.common.io.Files;
import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsAction;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.hdfs.DFSConfigKeys;
import org.apache.hadoop.io.DataOutputBuffer;
import org.apache.hadoop.net.NetUtils;
import org.apache.hadoop.registry.client.api.RegistryConstants;
import org.apache.hadoop.registry.client.api.RegistryOperations;
import org.apache.hadoop.registry.client.api.RegistryOperationsFactory;
import org.apache.hadoop.registry.client.binding.RegistryUtils;
import org.apache.hadoop.security.Credentials;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.security.authorize.AccessControlList;
import org.apache.hadoop.security.token.Token;
import org.apache.hadoop.yarn.api.ApplicationConstants;
import org.apache.hadoop.yarn.api.protocolrecords.GetApplicationsRequest;
import org.apache.hadoop.yarn.api.protocolrecords.UpdateApplicationTimeoutsRequest;
import org.apache.hadoop.yarn.api.records.*;
import org.apache.hadoop.yarn.client.api.AppAdminClient;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.client.api.YarnClientApplication;
import org.apache.hadoop.yarn.client.cli.ApplicationCLI;
import org.apache.hadoop.yarn.client.util.YarnClientUtils;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.ipc.YarnRPC;
import org.apache.hadoop.yarn.proto.ClientAMProtocol.CancelUpgradeRequestProto;
import org.apache.hadoop.yarn.proto.ClientAMProtocol.CompInstancesUpgradeRequestProto;
import org.apache.hadoop.yarn.proto.ClientAMProtocol.ComponentCountProto;
import org.apache.hadoop.yarn.proto.ClientAMProtocol.DecommissionCompInstancesRequestProto;
import org.apache.hadoop.yarn.proto.ClientAMProtocol.FlexComponentsRequestProto;
import org.apache.hadoop.yarn.proto.ClientAMProtocol.GetCompInstancesRequestProto;
import org.apache.hadoop.yarn.proto.ClientAMProtocol.GetCompInstancesResponseProto;
import org.apache.hadoop.yarn.proto.ClientAMProtocol.GetStatusRequestProto;
import org.apache.hadoop.yarn.proto.ClientAMProtocol.GetStatusResponseProto;
import org.apache.hadoop.yarn.proto.ClientAMProtocol.RestartServiceRequestProto;
import org.apache.hadoop.yarn.proto.ClientAMProtocol.StopRequestProto;
import org.apache.hadoop.yarn.proto.ClientAMProtocol.UpgradeServiceRequestProto;
import org.apache.hadoop.yarn.proto.ClientAMProtocol.UpgradeServiceResponseProto;
import org.apache.hadoop.yarn.service.ClientAMProtocol;
import org.apache.hadoop.yarn.service.ServiceMaster;
import org.apache.hadoop.yarn.service.api.records.ComponentContainers;
import org.apache.hadoop.yarn.service.api.records.Container;
import org.apache.hadoop.yarn.service.api.records.ContainerState;
import org.apache.hadoop.yarn.service.api.records.Component;
import org.apache.hadoop.yarn.service.api.records.ConfigFile;
import org.apache.hadoop.yarn.service.api.records.ConfigFile.TypeEnum;
import org.apache.hadoop.yarn.service.conf.SliderExitCodes;
import org.apache.hadoop.yarn.service.conf.YarnServiceConf;
import org.apache.hadoop.yarn.service.conf.YarnServiceConstants;
import org.apache.hadoop.yarn.service.containerlaunch.ClasspathConstructor;
import org.apache.hadoop.yarn.service.containerlaunch.JavaCommandLineBuilder;
import org.apache.hadoop.yarn.service.exceptions.BadClusterStateException;
import org.apache.hadoop.yarn.service.exceptions.BadConfigException;
import org.apache.hadoop.yarn.service.exceptions.ErrorStrings;
import org.apache.hadoop.yarn.service.exceptions.SliderException;
import org.apache.hadoop.yarn.service.provider.AbstractClientProvider;
import org.apache.hadoop.yarn.service.provider.ProviderUtils;
import org.apache.hadoop.yarn.service.utils.ServiceApiUtil;
import org.apache.hadoop.yarn.service.utils.ServiceRegistryUtils;
import org.apache.hadoop.yarn.service.utils.ServiceUtils;
import org.apache.hadoop.yarn.service.utils.SliderFileSystem;
import org.apache.hadoop.yarn.service.utils.ZookeeperUtils;
import org.apache.hadoop.yarn.util.DockerClientConfigHandler;
import org.apache.hadoop.yarn.util.Records;
import org.apache.hadoop.yarn.util.Times;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import static org.apache.hadoop.yarn.api.records.YarnApplicationState.*;
import static org.apache.hadoop.yarn.service.conf.YarnServiceConf.*;
import static org.apache.hadoop.yarn.service.utils.ServiceApiUtil.jsonSerDeser;
import static org.apache.hadoop.yarn.service.utils.ServiceUtils.*;

public class ServiceClient_actionUpgradeExpress_6_2_Test {

    private ServiceClient serviceClient;

    private Service mockService;

    private File mockFile;

    @Before
    public void setUp() {
        serviceClient = Mockito.spy(new ServiceClient());
        mockService = mock(Service.class);
        mockFile = mock(File.class);
    }

    @Test
    public void testActionUpgradeExpress() throws IOException, YarnException {
        String appName = "testApp";
        String path = "/path/to/service.json";
        doReturn(path).when(mockFile).getAbsolutePath();
        doReturn(mockService).when(serviceClient).loadAppJsonFromLocalFS(eq(path), eq(appName), isNull(), isNull());
        // Mock the Service object behavior
        doNothing().when(mockService).setState(ServiceState.UPGRADING_AUTO_FINALIZE);
        doReturn(ServiceState.UPGRADING_AUTO_FINALIZE).when(mockService).getState();
        // Mock actionUpgradeExpress(Service) behavior
        doReturn(0).when(serviceClient).actionUpgradeExpress(mockService);
        int result = serviceClient.actionUpgradeExpress(appName, mockFile);
        verify(serviceClient, times(1)).loadAppJsonFromLocalFS(eq(path), eq(appName), isNull(), isNull());
        verify(mockService, times(1)).setState(ServiceState.UPGRADING_AUTO_FINALIZE);
        verify(serviceClient, times(1)).actionUpgradeExpress(mockService);
        assertEquals(0, result);
    }
}
