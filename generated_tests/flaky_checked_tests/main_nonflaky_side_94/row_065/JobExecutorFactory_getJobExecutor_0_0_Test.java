package io.elasticjob.lite.executor;

import io.elasticjob.lite.api.ElasticJob;
import io.elasticjob.lite.api.dataflow.DataflowJob;
import io.elasticjob.lite.api.simple.SimpleJob;
import io.elasticjob.lite.exception.JobConfigurationException;
import io.elasticjob.lite.executor.JobFacade;
import io.elasticjob.lite.executor.JobExecutorFactory;
import io.elasticjob.lite.executor.type.DataflowJobExecutor;
import io.elasticjob.lite.executor.type.ScriptJobExecutor;
import io.elasticjob.lite.executor.type.SimpleJobExecutor;
import org.junit.Test;
import org.mockito.*;
import org.junit.Before;
import org.junit.After;
import org.junit.Assert;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

public class JobExecutorFactory_getJobExecutor_0_0_Test {

    @Test(expected = JobConfigurationException.class)
    public void testGetJobExecutorWithUnsupportedJobType() {
        ElasticJob unsupportedJob = mock(ElasticJob.class);
        JobFacade jobFacade = mock(JobFacade.class);
        JobExecutorFactory.getJobExecutor(unsupportedJob, jobFacade);
    }
}
