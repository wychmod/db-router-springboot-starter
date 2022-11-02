package com.wychmod.middleware.db.router;

import com.wychmod.middleware.db.router.annotation.DBRouter;
import org.junit.Test;

import java.lang.reflect.Method;

/**
 * @description:
 * @author: wychmod
 * @date: 2022/11/2
 */
public class ApiTest {

    @Test
    public void test_annotation() throws NoSuchMethodException {
        Class<IUserDao> iUserDaoClass = IUserDao.class;
        Method method = iUserDaoClass.getMethod("insertUser", String.class);

        DBRouter dbRouter = method.getAnnotation(DBRouter.class);

        System.out.println(dbRouter.key());

    }
}
