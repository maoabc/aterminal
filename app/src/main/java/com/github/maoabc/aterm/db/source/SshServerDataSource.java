package com.github.maoabc.aterm.db.source;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.github.maoabc.aterm.db.entities.SshServer;
import com.github.maoabc.util.AppExecutors;

import java.util.List;


public class SshServerDataSource {

    private final SshServerDao mDeviceDao;
    private final AppExecutors mExecutors;

    public SshServerDataSource(SshServerDao sshServerDao, AppExecutors executors) {
        this.mDeviceDao = sshServerDao;
        this.mExecutors = executors;
    }

    public LiveData<List<SshServer>> getSshServers() {
        final MutableLiveData<List<SshServer>> liveData = new MutableLiveData<>();
        mExecutors.diskIO().execute(() -> {
            List<SshServer> remoteDevices = mDeviceDao.getSshServers();
            liveData.postValue(remoteDevices);
        });
        return liveData;
    }

    public LiveData<Boolean> addSshServer(SshServer sshServer) {
        final MutableLiveData<Boolean> liveData = new MutableLiveData<>();
        mExecutors.diskIO().execute(() -> {
            mDeviceDao.insertSshServer(sshServer);
            liveData.postValue(true);
        });
        return liveData;
    }

    public LiveData<Boolean> addSshServers(List<SshServer> sshServers) {
        final MutableLiveData<Boolean> liveData = new MutableLiveData<>();
        mExecutors.diskIO().execute(() -> {
            mDeviceDao.insertSshServers(sshServers);
            liveData.postValue(true);
        });
        return liveData;
    }

    public LiveData<Boolean> delete(@NonNull SshServer sshServer) {
        final MutableLiveData<Boolean> liveData = new MutableLiveData<>();
        mExecutors.diskIO().execute(() -> {
            mDeviceDao.deleteById(sshServer.getId());
            liveData.postValue(true);
        });
        return liveData;
    }

    public void deleteAll() {
        mExecutors.diskIO().execute(mDeviceDao::deleteAll);
    }
}
