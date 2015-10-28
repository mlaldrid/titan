package com.thinkaurelius.titan.hadoop.compat.h2;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;

import com.thinkaurelius.titan.hadoop.config.job.AbstractDistCacheConfigurer;
import com.thinkaurelius.titan.hadoop.config.job.JobClasspathConfigurer;

public class DistCacheConfigurer extends AbstractDistCacheConfigurer implements JobClasspathConfigurer {

    public DistCacheConfigurer(String mapredJarFilename) {
        super(mapredJarFilename);
    }

    @Override
    public void configure(Job job) throws IOException {

        Configuration conf = job.getConfiguration();
        FileSystem localFS = FileSystem.getLocal(conf);
        FileSystem jobFS = FileSystem.get(conf);

        System.out.println("LOOK HERE");
        System.out.println("localFS: " + localFS.toString());
        System.out.println("jobFS: " + jobFS.toString());
        for (Path p : getLocalPaths()) {
            System.out.println("path: " + p.toString());
            Path stagedPath = uploadFileIfNecessary(localFS, p, jobFS);
            System.out.println("stagedPath: " + stagedPath.toString());
            // Calling this method decompresses the archive and makes Hadoop
            // handle its classfiles individually.  This leads to crippling
            // overhead times (10+ seconds) even with the LocalJobRunner
            // courtesy of o.a.h.yarn.util.FSDownload.changePermissions
            // copying and chmodding each classfile copy file individually.
            //job.addArchiveToClassPath(p);
            // Just add the compressed archive instead:
            job.addFileToClassPath(stagedPath);
        }

        // We don't really need to set a mapred job jar here,
        // but doing so suppresses a warning
        String mj = getMapredJar();
        if (null != mj)
            job.setJar(mj);
    }
}
