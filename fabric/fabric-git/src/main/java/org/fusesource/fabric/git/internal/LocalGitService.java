/**
 * Copyright (C) FuseSource, Inc.
 * http://fusesource.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fusesource.fabric.git.internal;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Service;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.fusesource.fabric.git.GitListener;
import org.fusesource.fabric.service.support.AbstractComponent;
import org.osgi.service.component.ComponentContext;

/**
 * A local stand alone git repository
 */
@Component(name = "org.fusesource.fabric.git.local", description = "Local Git Service", immediate = true)
@Service(GitService.class) // @Ref Done
public class LocalGitService extends AbstractComponent implements GitService {
    public static final String DEFAULT_GIT_PATH = File.separator + "git" + File.separator + "fabric";
    public static final String DEFAULT_LOCAL_LOCATION = System.getProperty("karaf.data") + DEFAULT_GIT_PATH;

    private final File localRepo = new File(DEFAULT_LOCAL_LOCATION);
    private final List<GitListener> callbacks = new CopyOnWriteArrayList<GitListener>();

    private String remoteUrl;
    private Git git;

    @Activate
    synchronized void activate(ComponentContext context) throws IOException {
        activateComponent(context);
        try {
            if (!localRepo.exists() && !localRepo.mkdirs()) {
                throw new IOException("Failed to create local repository");
            }
            git = openOrInit(localRepo);
        } catch (IOException ex) {
            deactivateComponent();
            throw ex;
        } catch (RuntimeException rte) {
            deactivateComponent();
            throw rte;
        }
    }

    @Deactivate
    synchronized void deactivate() {
        deactivateComponent();
    }

    @Override
    public Git get() throws IOException {
        return git;
    }

    @Override
    public String getRemoteUrl() {
        assertValid();
        return remoteUrl;
    }

    @Override
    public void onRemoteChanged(String remoteUrl) {
        assertValid();
        this.remoteUrl = remoteUrl;
        synchronized (callbacks) {
            for (GitListener listener : callbacks) {
                listener.onRemoteUrlChanged(remoteUrl);
            }
        }
    }

    @Override
    public void addRemoteChangeListener(GitListener callback) {
        assertValid();
        synchronized (callbacks) {
            callbacks.add(callback);
        }
    }

    @Override
    public void removeRemoteChangeListener(GitListener callback) {
        assertValid();
        synchronized (callbacks) {
            callbacks.remove(callback);
        }
    }

    private Git openOrInit(File repo) throws IOException {
        try {
            return Git.open(repo);
        } catch (RepositoryNotFoundException e) {
            try {
                Git git = Git.init().setDirectory(localRepo).call();
                git.commit().setMessage("First Commit").setCommitter("fabric", "user@fabric").call();
                return git;
            } catch (GitAPIException ex) {
                throw new IOException(ex);
            }
        }
    }
}
