package com.wychmod.middleware.db.router;

import com.wychmod.middleware.db.router.annotation.DBRouter;

/**
 * @description:
 * @author: wychmod
 * @date: 2022/11/2
 */
public interface IUserDao {

    @DBRouter(key = "userId")
    void insertUser(String req);
}
