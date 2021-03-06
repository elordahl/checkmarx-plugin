package com.checkmarx.jenkins;

import com.checkmarx.jenkins.cryptography.CryptographicCallable;
import com.checkmarx.jenkins.folder.FoldersScanner;
import com.checkmarx.jenkins.opensourceanalysis.*;
import com.checkmarx.jenkins.web.client.RestClient;
import com.checkmarx.jenkins.web.model.AnalyzeRequest;
import com.checkmarx.jenkins.web.model.GetOpenSourceSummaryRequest;
import com.checkmarx.jenkins.web.model.GetOpenSourceSummaryResponse;
import hudson.FilePath;
import hudson.remoting.VirtualChannel;
import mockit.Invocation;
import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;

/**
 * @author tsahi
 * @since 02/02/16
 */
@RunWith(JMockit.class)
public class OpenSourceAnalyzerServiceTests {

    @Test
    public void analyze_noIncludes_nothingShouldHappen() throws IOException, InterruptedException {

        new MockUp<FoldersScanner>() {
            @Mock(invocations = 0)
            void $init(List<String> libIncludes, List<String> libExcludes) {
            }
        };

        DependencyFolder folders = new DependencyFolder("", "test");
        OpenSourceAnalyzerService service = new OpenSourceAnalyzerService(null, folders, null, 0, Logger.getLogger(getClass()), null);
        service.analyze();
    }

    @Test
    public void analyze_emptyDependencies_logNoDependencies() throws IOException, InterruptedException {

        new MockUp<OpenSourceAnalyzerService>() {
            @Mock
            Collection<DependencyInfo> getDependenciesFromFolders(Invocation invocation) throws IOException, InterruptedException {
                return new ArrayList<>();
            }
        };
        new MockUp<FoldersScanner>() {
        };

        final Set infoMessages= new HashSet();
        new MockUp<Logger>() {
            @Mock
            void info(Object message) {
                infoMessages.add(message);
            }
        };
        new MockUp<CxWebService>() {
            @Mock
            void $init(String serverUrl) {
            }
            @Mock
            Boolean isOsaLicenseValid(){
                return true;
            }
        };

        DependencyFolder folders = new DependencyFolder("test2", "");
        OpenSourceAnalyzerService service = new OpenSourceAnalyzerService(null, folders, null, 0, Logger.getLogger(getClass()), new CxWebService(null));

        service.analyze();

        assertTrue(infoMessages.contains("No dependencies found"));
    }

    @Test
    public void analyze_withDependencies_NoError() throws IOException, InterruptedException {

        new MockUp<OpenSourceAnalyzerService>() {
            @Mock
            Collection<DependencyInfo> getDependenciesFromFolders(Invocation invocation) throws IOException, InterruptedException {
                DependencyInfo info = new DependencyInfo();
                info.setFilePath(new FilePath(new File("test")));
                return new ArrayList<DependencyInfo>(Arrays.asList(info));
            }
        };
        new MockUp<FoldersScanner>() {
        };
        new MockUp<Logger>() {
            void info(Object message) {
            }
        };
        new MockUp<CryptographicCallable>() {
            @Mock
            String invoke(File f, VirtualChannel channel) throws IOException, InterruptedException {
                return "a94a8fe5ccb19ba61c4c0873d391e987982fbbd3";
            }
        };
        new MockUp<RestClient>() {
            @Mock
            void analyzeOpenSources(AnalyzeRequest request) {
            }
        };
        new MockUp<CxWebService>() {
            @Mock
            void $init(String serverUrl) {
            }
            @Mock
            Boolean isOsaLicenseValid(){
                return true;
            }
        };

        DependencyFolder folders = new DependencyFolder("test2", "");
        OpenSourceAnalyzerService service = new OpenSourceAnalyzerService(null, folders, new RestClient("", null), 0, Logger.getLogger(getClass()), new CxWebService(null));

        service.analyze();
    }

    @Test
    public void analyze_RunAnalysisWithoutErrors_StartAndEndRunMessageShouldBeWritten() throws IOException, InterruptedException {

        new MockUp<OpenSourceAnalyzerService>() {
            @Mock
            Collection<DependencyInfo> getDependenciesFromFolders(Invocation invocation) throws IOException, InterruptedException {
                DependencyInfo info = new DependencyInfo();
                info.setFilePath(new FilePath(new File("test")));
                return new ArrayList<DependencyInfo>(Arrays.asList(info));
            }
            @Mock
            GetOpenSourceSummaryResponse getOpenSourceSummary()
            {
                return new GetOpenSourceSummaryResponse();
            }
        };
        new MockUp<FoldersScanner>() {
        };
        final Set infoMessages= new HashSet();
        new MockUp<Logger>() {
            @Mock
            void info(Object message) {
                infoMessages.add(message);
            }
        };
        new MockUp<CryptographicCallable>() {
            @Mock
            String invoke(File f, VirtualChannel channel) throws IOException, InterruptedException {
                return "a94a8fe5ccb19ba61c4c0873d391e987982fbbd3";
            }
        };
        new MockUp<RestClient>() {
            @Mock
            void analyzeOpenSources(AnalyzeRequest request) {
            }
            @Mock
            GetOpenSourceSummaryResponse getOpenSourceSummary(GetOpenSourceSummaryRequest request){
                return new GetOpenSourceSummaryResponse();
            }
        };
        new MockUp<CxWebService>() {
            @Mock
            void $init(String serverUrl) {
            }
            @Mock
            Boolean isOsaLicenseValid(){
                return true;
            }
        };


        DependencyFolder folders = new DependencyFolder("test2", "");
        OpenSourceAnalyzerService service = new OpenSourceAnalyzerService(null, folders, new RestClient("", null), 0, Logger.getLogger(getClass()), new CxWebService(null));

        service.analyze();

        assertTrue(infoMessages.contains("OSA (open source analysis) Run has started"));
        assertTrue(infoMessages.contains("OSA (open source analysis) Run has finished successfully"));
    }

    @Test
    public void openSourceAnalyzerServiceTests_noLicense_logNoLicense() throws IOException, InterruptedException {

        new MockUp<OpenSourceAnalyzerService>() {
            @Mock
            Collection<DependencyInfo> getDependenciesFromFolders(Invocation invocation) throws IOException, InterruptedException {
                DependencyInfo info = new DependencyInfo();
                info.setFilePath(new FilePath(new File("test")));
                return new ArrayList<DependencyInfo>(Arrays.asList(info));
            }
        };
        new MockUp<Logger>() {
            void error(Object message) {
                assertTrue(message.toString().contains(OpenSourceAnalyzerService.NO_LICENSE_ERROR));
            }
        };
        new MockUp<CxWebService>() {
            @Mock
            void $init(String serverUrl) {
            }
            @Mock
            Boolean isOsaLicenseValid(){
                return false;
            }
        };

        DependencyFolder folders = new DependencyFolder("test2", "");
        OpenSourceAnalyzerService service = new OpenSourceAnalyzerService(null, folders, null, 0, Logger.getLogger(getClass()), new CxWebService(null));

        service.analyze();

    }
}
