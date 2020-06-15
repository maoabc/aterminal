package com.github.maoabc.aterm.db.source;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.github.maoabc.aterm.db.entities.SshServer;

import java.util.List;


/**
 * Created by mao on 18-3-7.
 */

@Dao
public interface SshServerDao {
    @Query("select * from ssh_servers order by order_index asc")
    List<SshServer> getSshServers();

    @Query("select * from ssh_servers where _id = :id")
    SshServer getSshServer(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSshServer(SshServer sshServer);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSshServers(List<SshServer> sshServers);


    @Query("delete from ssh_servers where _id = :id")
    void deleteById(String id);

    @Query("delete from ssh_servers")
    void deleteAll();
}
