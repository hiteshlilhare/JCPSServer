package com.github.hiteshlilhare.jcpss;

import com.github.hiteshlilhare.jcpss.bean.GitHubRelease;
import com.github.hiteshlilhare.jcpss.bean.ReleaseMonitorTimerMap;
import com.github.hiteshlilhare.jcpss.bean.RepoDetail;
import com.github.hiteshlilhare.jcpss.db.DAOFactory;
import com.github.hiteshlilhare.jcpss.db.DatabaseDAOAdapter;
import com.github.hiteshlilhare.jcpss.util.ReleaseMonitorTimerTask;
import com.github.hiteshlilhare.jcpss.util.Util;
import java.io.File;
import java.io.IOException;
import java.security.Security;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class JCPSServletApplication {

    private static final org.slf4j.Logger logger
            = LoggerFactory.getLogger(JCPSServletApplication.class);

    public static final String PUB_URL = "https://3fd2686e.ngrok.io/jcpss/";

    public static final String REMOTE_REPO_URL = "https://github.com/hiteshlilhare/JCAppletStore.git";

    public static final String DATABASE = DAOFactory.MYSQL;

    public static final String REPO_STATUS_TO_MONITOR
            = RepoDetail.Status.PreRegistered.toString();

    public static final String RELEASE_STATUS_TO_MONITOR
            = GitHubRelease.Status.PreVerified.toString();

    public static void main(String[] args) {
        Security.addProvider(new BouncyCastleProvider());
        ApplicationContext applicationContext
                = SpringApplication.run(JCPSServletApplication.class, args);
        boolean status = Util.createDirectoryStructureIfNotExist();
        if (!status) {
            logger.error("createDirectoryStructureIfNotExist returns false");
            System.exit(0);
        }
        StatusMessage statusMessage = new StatusMessage();
        DatabaseDAOAdapter databaseDAOAdapter
                = DAOFactory.getDatabaseDAO(JCPSServletApplication.DATABASE);
        //Create Database tables.
        status = databaseDAOAdapter.createTablesIfNotExist();
        if (!status) {
            logger.error("DAOFactory.getDatabaseDAO returns false");
            System.exit(0);
        }
        ArrayList<RepoDetail> repoDetails = new ArrayList<>();
        databaseDAOAdapter.getListOfRepoDetailsWRTStatus(
                REPO_STATUS_TO_MONITOR,
                repoDetails,
                statusMessage);
        logger.info(statusMessage.getMessage());
        if (statusMessage.getCode() == StatusMessage.Code.SUCCESS) {
            //create Release Monitor Timer Task 
            for (RepoDetail repoDetail : repoDetails) {
                ReleaseMonitorTimerTask task
                        = new ReleaseMonitorTimerTask(repoDetail);
                ReleaseMonitorTimerMap.getInstance().addTimer(task);
            }
        }
        //Validate Applet Store repository.
        validateJCAppletStoreRepository();

    }
//        String releaseURL = "https://api.github.com/repos/sid062010/Test/releases";
//        getListOfGitRepoReleases(releaseURL);
//getPublicKey("0x147C78EEAB15A8F0760A3C5171F886A25C75A07D", new StatusMessage());

    /**
     * Validate Applet Store directory.
     *
     * @throws IOException
     * @throws GitAPIException
     */
    private static void validateJCAppletStoreRepository() {
        File appletStoreDir = new File(JCPSSCOnstants.JCPS_SRV_DIR
                + "/" + JCPSSCOnstants.JCPS_REMOTE_REPO + "/.git");
        if (appletStoreDir.exists()) {
            Git git = null;
            try {
                Map<String, Ref> remoteRef = Git.lsRemoteRepository()
                        .setRemote(REMOTE_REPO_URL)
                        .callAsMap();
                Repository repository = new FileRepositoryBuilder().
                        setGitDir(appletStoreDir).findGitDir().build();
                Map<String, Ref> localRef = repository.getAllRefs();
                Set<String> keys = remoteRef.keySet();
                boolean flag = true;
                for (String key : keys) {
                    if (remoteRef.get(key).getObjectId() == null) {
                        continue;
                    }
                    if (localRef.get(key).getObjectId() == null) {
                        flag = false;
                        break;
                    }
                    String localObjectID = localRef.get(key).getObjectId().getName();
                    String remoteObjectID = remoteRef.get(key).getObjectId().getName();
                    if (localRef.get(key) == null
                            || !localObjectID.equals(remoteObjectID)) {
                        flag = false;
                        break;
                    }
                }
                if (!flag) {
                    File jcpsRemoteRepo = new File(JCPSSCOnstants.JCPS_SRV_DIR
                            + "/" + JCPSSCOnstants.JCPS_REMOTE_REPO);
                    FileUtils.deleteDirectory(jcpsRemoteRepo);
                    if (jcpsRemoteRepo.exists()) {
                        logger.info("Unable to delete " + jcpsRemoteRepo + " remote repo directory");
                        System.exit(0);
                    }
                    if (jcpsRemoteRepo.mkdirs()) {
                        logger.info("Unable to create " + jcpsRemoteRepo + " remote repo directory");
                        System.exit(0);
                    }
                    git = Git.cloneRepository().setBranch("refs/heads/master")
                            .setURI(REMOTE_REPO_URL).setDirectory(jcpsRemoteRepo).call();
                    if (git == null) {
                        logger.error("Unable to clone " + REMOTE_REPO_URL + " applet store directory");
                        System.exit(0);
                    }
                }
            } catch (GitAPIException | IOException ex) {
                logger.error("validateJCAppletStoreRepository", ex);
                System.exit(0);
            } finally {
                if (git != null) {
                    git.close();
                }
            }
        } else {
            Git git = null;
            try {
                System.out.println("Not Exist");
                appletStoreDir = new File(JCPSSCOnstants.JCPS_SRV_DIR
                        + "/" + JCPSSCOnstants.JCPS_REMOTE_REPO);
                git = Git.cloneRepository().setBranch("refs/heads/master")
                        .setURI(REMOTE_REPO_URL).setDirectory(appletStoreDir).call();
                if (git == null) {
                    logger.error("Unable to clone " + REMOTE_REPO_URL + " applet store directory");
                    System.exit(0);
                }
            } catch (GitAPIException ex) {
                logger.error("validateJCAppletStoreRepository", ex);
                System.exit(0);
            } finally {
                if (git != null) {
                    git.close();
                }
            }
        }
    }

}
//@Component
//class MyCustomizer /*implements EmbeddedServletContainerCustomizer*/ {
//    
//}

