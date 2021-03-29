import redis.clients.jedis.*;
import redis.clients.util.Hashing;
import redis.clients.util.Sharded;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClientJedis {
    public static void main(String[] args) {

        /**
         * 简单使用
         */
        // 创建 jedis 对象，指定 redis 服务 host、port
        Jedis jedis = null;
        try {
            // 1：从 连接池 获取 连接对象
            //  如果访问 redis 服务需要密码，则指定密码
            jedis = new Jedis("toshiba",6379);
            jedis.auth("passwd");
            // 2：访问 redis 服务
            String key = "qwbqk:10014:385:-1:17968";
            String value = jedis.get(key);
            System.out.println("key: " + key + " ,\n" + "value：" + value);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 3：使用完关闭 redis 服务
            if (jedis != null) jedis.close();
        }


        /**
         * 使用 JedisPool连接池，搭配 redis-cluster 实现 HA
         *
         * 适用于 redis-cluster，3主3从
         */
        // 1：创建 连接池配置对象
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(30);    // 指定最大空闲连接数
        jedisPoolConfig.setMaxTotal(100);    // 指定最大连接数
        jedisPoolConfig.setMaxWaitMillis(60000);    // 指定最大等待时间
        // 2：创建 连接池对象，关联 连接池配置对象
        JedisPool jedisPool = new JedisPool(jedisPoolConfig,
                "toshiba",6379, 60000,"passwd");
        Jedis jedis = null;
        try {
            // 3：从 连接池 获取 连接对象
            jedis = jedisPool.getResource();
            // 4：访问 redis 服务
            String key = "qwbqk:10014:385:-1:17968";
            String value = jedis.get(key);
            System.out.println("key: " + key + " ,\n" + "value：" + value);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 5：使用完关闭 redis 服务
            if (jedis != null) jedis.close();
        }


        /**
         * 使用 JedisSentinelPool 实现 HA
         *
         * 适用于 redis-sentinel，1主1从+3哨兵
         * 其中，主节点名称 默认 mymaster
         */
        // 1：创建 连接池配置对象
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(30);
        jedisPoolConfig.setMaxIdle(300);
        jedisPoolConfig.setMaxWaitMillis(60000);
        // 2：创建 哨兵集合对象
        Set<String> jedisSentinels = new HashSet<String>();
        jedisSentinels.add("toshiba:26379");
        jedisSentinels.add("toshiba:26380");
        jedisSentinels.add("toshiba:26381");
        // 3：创建 哨兵连接池对象，关联 主节点名称、哨兵集合对象、连接池配置对象
        JedisSentinelPool jedisSentinelPool = new JedisSentinelPool("mymaster",jedisSentinels,jedisPoolConfig,60000);
        Jedis jedis = null;
        try {
            // 4：从 连接池 获取 连接对象
            jedis = jedisSentinelPool.getResource();
            // 5：访问 redis 服务
            String key = "qwbqk:10014:385:-1:17968";
            String value = jedis.get(key);
            System.out.println("key: " + key + " ,\n" + "value：" + value);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 6：使用完关闭 redis 服务
            if (jedis != null) jedis.close();
        }

        /**
         * 用 ShardedJedisPool 实现 客户端分片
         *
         * 适用于 松散无组织的 多个 redis-single
         */
        // 1：创建 连接池配置对象
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(30);
        jedisPoolConfig.setMaxIdle(300);
        jedisPoolConfig.setMaxWaitMillis(60000);
        jedisPoolConfig.setTestOnBorrow(true);
        // 2：创建 客户端分片 结合对象
        List<JedisShardInfo> jedisInfoList = new ArrayList<>(2);
        //  模拟 1 台正常的 redis 服务主机
        jedisInfoList.add(new JedisShardInfo("toshiba", 11111));
        //  模拟 1 台发生宕机故障的 redis 服务主机，实际上不存在该主机
//        jedisInfoList.add(new JedisShardInfo("toshiba_failover", 11111));
        ShardedJedisPool shardedJedisPool = new ShardedJedisPool(jedisPoolConfig,
                jedisInfoList, Hashing.MURMUR_HASH, Sharded.DEFAULT_KEY_TAG_PATTERN);
        ShardedJedis jedis = null;
        try {
            // 4：从 连接池 获取 连接对象
            jedis = shardedJedisPool.getResource();
            // 5：访问 redis 服务
            String key = "qwbqk:10014:385:-1:17968";
            String value = jedis.get(key);
            System.out.println("key: " + key + " ,\n" + "value：" + value);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 6：使用完关闭 redis 服务
            if (jedis != null) jedis.close();
        }

    }
}