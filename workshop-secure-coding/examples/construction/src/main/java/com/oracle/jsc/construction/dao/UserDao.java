package com.oracle.jsc.construction.dao;

import java.io.Closeable;

public interface UserDao extends Closeable {
    User getUser(String userId) throws UserStateException;
}
