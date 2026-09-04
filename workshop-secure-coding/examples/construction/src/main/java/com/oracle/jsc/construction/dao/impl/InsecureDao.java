package com.oracle.jsc.construction.dao.impl;

import java.io.Closeable;

import com.oracle.jsc.construction.dao.User;
import com.oracle.jsc.construction.dao.UserDao;
import com.oracle.jsc.construction.dao.UserStateException;

public final class InsecureDao implements Closeable, UserDao {
    private static final boolean ADMIN = true;

    public InsecureDao() throws UserStateException {
        throw new UserStateException("Unable to connect to the database or find the backup config.");
    }

    public User getUser(String userId) {
        // retrieve by user ID, do whatever checks are needed, return a spiffy immutable user.
        return new User(userId, ADMIN);
    }

    @Override
    public void close() {
        // success!
    }

}
