package com.oracle.jsc.construction.dao;

import com.oracle.jsc.construction.dao.impl.SecureDao;

public final class SecureDaoFactory {
    private static final class NoAdminsDao extends SecureDao {
        @Override
        public User getUser(String userId) {
            return new User(userId, ! ADMIN);
        }
    }

    private SecureDaoFactory() {
    }

    public static UserDao getUserDao() {
        try (SecureDao dao = new SecureDao()) {
            if (dao.isValid()) {
                return dao;
            }
        }

        System.err.println("Unable to connect to the user database. Returning a DAO that only produces guest users.");
        new UserStateException().printStackTrace(System.err);
        return new NoAdminsDao();
    }

}
