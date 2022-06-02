package com.rxf113.vertx_demo.conf;

import io.vertx.core.Vertx;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;


/**
 * @author rxf113
 */
public class MysqlProcessor implements InitProcessor {

  public MySQLPool pool;

  @Override
  public boolean init(Vertx vertx) {
    MySQLConnectOptions connectOptions = new MySQLConnectOptions()
      .setPort(3306)
      .setHost("127.0.0.1")
      .setDatabase("rxf113")
      .setUser("root")
      .setPassword("1131310577");

// Pool options
    PoolOptions poolOptions = new PoolOptions()
      .setMaxSize(5);

// Create the pooled client
    pool = MySQLPool.pool(vertx, connectOptions, poolOptions);

// Get a connection from the pool
//    pool.getConnection().compose(conn -> {
//      System.out.println("Got a connection from the pool");
//
//      // All operations execute on the same connection
//      return conn
//        .query("SELECT * FROM users WHERE id='julien'")
//        .execute()
//        .compose(res -> conn
//          .query("SELECT * FROM users WHERE id='emad'")
//          .execute())
//        .onComplete(ar -> {
//          // Release the connection to the pool
//          conn.close();
//        });
//    }).onComplete(ar -> {
//      if (ar.succeeded()) {
//
//        System.out.println("Done");
//      } else {
//        System.out.println("Something went wrong " + ar.cause().getMessage());
//      }
//    });

    return false;
  }
}
