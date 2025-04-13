package com.redis.plugin.service;

import com.intellij.openapi.components.Service;
import com.redis.plugin.model.RedisCommand;
import com.redis.plugin.model.RedisCommandCategory;
import com.redis.plugin.model.RedisCommandComplexity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service that provides a catalog of Redis commands
 */
@Service
public final class RedisCommandCatalog {
    private final List<RedisCommandCategory> categories;
    private final Map<String, RedisCommand> commandsByName;
    
    public RedisCommandCatalog() {
        categories = new ArrayList<>();
        commandsByName = new HashMap<>();
        initializeCommands();
    }
    
    public List<RedisCommandCategory> getCategories() {
        return categories;
    }
    
    public RedisCommand getCommand(String name) {
        return commandsByName.get(name.toLowerCase());
    }
    
    private void initializeCommands() {
        // Create categories
        RedisCommandCategory stringCategory = new RedisCommandCategory("Strings", "Operations on string values");
        RedisCommandCategory listCategory = new RedisCommandCategory("Lists", "Operations on lists of elements");
        RedisCommandCategory hashCategory = new RedisCommandCategory("Hashes", "Operations on hash tables");
        RedisCommandCategory setCategory = new RedisCommandCategory("Sets", "Operations on sets of elements");
        RedisCommandCategory sortedSetCategory = new RedisCommandCategory("Sorted Sets", "Operations on sorted sets");
        RedisCommandCategory keyCategory = new RedisCommandCategory("Keys", "Operations on keys and databases");
        RedisCommandCategory serverCategory = new RedisCommandCategory("Server", "Server management commands");
        RedisCommandCategory pubSubCategory = new RedisCommandCategory("Pub/Sub", "Publish/Subscribe messaging");
        RedisCommandCategory transactionCategory = new RedisCommandCategory("Transactions", "Transaction-related commands");
        
        categories.add(stringCategory);
        categories.add(listCategory);
        categories.add(hashCategory);
        categories.add(setCategory);
        categories.add(sortedSetCategory);
        categories.add(keyCategory);
        categories.add(serverCategory);
        categories.add(pubSubCategory);
        categories.add(transactionCategory);
        
        // Add string commands
        addStringCommands(stringCategory);
        
        // Add list commands
        addListCommands(listCategory);
        
        // Add hash commands
        addHashCommands(hashCategory);
        
        // Add set commands
        addSetCommands(setCategory);
        
        // Add sorted set commands
        addSortedSetCommands(sortedSetCategory);
        
        // Add key commands
        addKeyCommands(keyCategory);
        
        // Add server commands
        addServerCommands(serverCategory);
        
        // Add pub/sub commands
        addPubSubCommands(pubSubCategory);
        
        // Add transaction commands
        addTransactionCommands(transactionCategory);
    }
    
    private void addStringCommands(RedisCommandCategory category) {
        // GET command
        RedisCommand getCommand = new RedisCommand(
                "get",
                "GET key",
                "Returns the value of a key"
        );
        getCommand.setComplexity(RedisCommandComplexity.O1);
        getCommand.setSince("1.0.0");
        getCommand.addParameter("key - The key to retrieve the value for");
        getCommand.addReturnValue("Bulk string reply: the value of the key, or nil if the key does not exist");
        getCommand.addExample("GET mykey");
        
        // SET command
        RedisCommand setCommand = new RedisCommand(
                "set",
                "SET key value [EX seconds | PX milliseconds | EXAT timestamp | PXAT milliseconds-timestamp | KEEPTTL] [NX | XX] [GET]",
                "Sets the value of a key"
        );
        setCommand.setComplexity(RedisCommandComplexity.O1);
        setCommand.setSince("1.0.0");
        setCommand.addParameter("key - The key to set");
        setCommand.addParameter("value - The value to set");
        setCommand.addParameter("EX seconds - Set the specified expire time, in seconds");
        setCommand.addParameter("PX milliseconds - Set the specified expire time, in milliseconds");
        setCommand.addParameter("EXAT timestamp - Set the specified Unix time at which the key will expire, in seconds");
        setCommand.addParameter("PXAT milliseconds-timestamp - Set the specified Unix time at which the key will expire, in milliseconds");
        setCommand.addParameter("KEEPTTL - Retain the time to live associated with the key");
        setCommand.addParameter("NX - Only set the key if it does not already exist");
        setCommand.addParameter("XX - Only set the key if it already exists");
        setCommand.addParameter("GET - Return the old value stored at key, or nil if the key did not exist");
        setCommand.addReturnValue("Simple string reply: OK if SET was executed correctly");
        setCommand.addReturnValue("Null reply: key was not set (NX or XX condition not met)");
        setCommand.addReturnValue("Bulk string reply: the old value when using GET");
        setCommand.addExample("SET mykey \"Hello\"");
        setCommand.addExample("SET mykey \"Hello\" EX 10");
        setCommand.addExample("SET mykey \"Hello\" PX 10000");
        setCommand.addExample("SET mykey \"Hello\" NX");
        setCommand.addExample("SET mykey \"Hello\" XX GET");
        
        // APPEND command
        RedisCommand appendCommand = new RedisCommand(
                "append",
                "APPEND key value",
                "Append a value to a key"
        );
        appendCommand.setComplexity(RedisCommandComplexity.O1);
        appendCommand.setSince("2.0.0");
        appendCommand.addParameter("key - The key to append to");
        appendCommand.addParameter("value - The value to append");
        appendCommand.addReturnValue("Integer reply: the length of the string after the append operation");
        appendCommand.addExample("APPEND mykey \"Hello\"");
        appendCommand.addExample("APPEND mykey \" World\"");
        
        // INCR command
        RedisCommand incrCommand = new RedisCommand(
                "incr",
                "INCR key",
                "Increments the integer value of a key by one"
        );
        incrCommand.setComplexity(RedisCommandComplexity.O1);
        incrCommand.setSince("1.0.0");
        incrCommand.addParameter("key - The key to increment");
        incrCommand.addReturnValue("Integer reply: the value of key after the increment");
        incrCommand.addExample("INCR mykey");
        
        // DECR command
        RedisCommand decrCommand = new RedisCommand(
                "decr",
                "DECR key",
                "Decrements the integer value of a key by one"
        );
        decrCommand.setComplexity(RedisCommandComplexity.O1);
        decrCommand.setSince("1.0.0");
        decrCommand.addParameter("key - The key to decrement");
        decrCommand.addReturnValue("Integer reply: the value of key after the decrement");
        decrCommand.addExample("DECR mykey");
        
        // GETSET command
        RedisCommand getsetCommand = new RedisCommand(
                "getset",
                "GETSET key value",
                "Sets the string value of a key and returns its old value"
        );
        getsetCommand.setComplexity(RedisCommandComplexity.O1);
        getsetCommand.setSince("1.0.0");
        getsetCommand.addParameter("key - The key to update");
        getsetCommand.addParameter("value - The new value to set");
        getsetCommand.addReturnValue("Bulk string reply: the old value stored at key, or nil if the key did not exist");
        getsetCommand.addExample("GETSET mykey \"Hello\"");
        getsetCommand.addExample("GETSET mykey \"World\"");
        
        // MGET command
        RedisCommand mgetCommand = new RedisCommand(
                "mget",
                "MGET key [key ...]",
                "Gets the values of all the given keys"
        );
        mgetCommand.setComplexity(RedisCommandComplexity.ON);
        mgetCommand.setSince("1.0.0");
        mgetCommand.addParameter("key - The first key to get");
        mgetCommand.addParameter("key ... - Additional keys to get");
        mgetCommand.addReturnValue("Array reply: list of values at the specified keys");
        mgetCommand.addExample("MGET key1 key2 key3");
        
        // MSET command
        RedisCommand msetCommand = new RedisCommand(
                "mset",
                "MSET key value [key value ...]",
                "Sets multiple keys to multiple values"
        );
        msetCommand.setComplexity(RedisCommandComplexity.ON);
        msetCommand.setSince("1.0.1");
        msetCommand.addParameter("key - The first key to set");
        msetCommand.addParameter("value - The value to set for the first key");
        msetCommand.addParameter("key value ... - Additional key-value pairs");
        msetCommand.addReturnValue("Simple string reply: always OK");
        msetCommand.addExample("MSET key1 \"Hello\" key2 \"World\"");
        
        // Register commands
        category.addCommand(getCommand);
        category.addCommand(setCommand);
        category.addCommand(appendCommand);
        category.addCommand(incrCommand);
        category.addCommand(decrCommand);
        category.addCommand(getsetCommand);
        category.addCommand(mgetCommand);
        category.addCommand(msetCommand);
        
        // Add to command map
        commandsByName.put(getCommand.getName(), getCommand);
        commandsByName.put(setCommand.getName(), setCommand);
        commandsByName.put(appendCommand.getName(), appendCommand);
        commandsByName.put(incrCommand.getName(), incrCommand);
        commandsByName.put(decrCommand.getName(), decrCommand);
        commandsByName.put(getsetCommand.getName(), getsetCommand);
        commandsByName.put(mgetCommand.getName(), mgetCommand);
        commandsByName.put(msetCommand.getName(), msetCommand);
    }
    
    private void addListCommands(RedisCommandCategory category) {
        // LPUSH command
        RedisCommand lpushCommand = new RedisCommand(
                "lpush",
                "LPUSH key element [element ...]",
                "Prepends one or multiple elements to a list"
        );
        lpushCommand.setComplexity(RedisCommandComplexity.O1);
        lpushCommand.setSince("1.0.0");
        lpushCommand.addParameter("key - The key of the list");
        lpushCommand.addParameter("element - The element to prepend");
        lpushCommand.addParameter("element ... - Additional elements to prepend");
        lpushCommand.addReturnValue("Integer reply: the length of the list after the push operation");
        lpushCommand.addExample("LPUSH mylist \"world\"");
        lpushCommand.addExample("LPUSH mylist \"hello\"");
        
        // RPUSH command
        RedisCommand rpushCommand = new RedisCommand(
                "rpush",
                "RPUSH key element [element ...]",
                "Appends one or multiple elements to a list"
        );
        rpushCommand.setComplexity(RedisCommandComplexity.O1);
        rpushCommand.setSince("1.0.0");
        rpushCommand.addParameter("key - The key of the list");
        rpushCommand.addParameter("element - The element to append");
        rpushCommand.addParameter("element ... - Additional elements to append");
        rpushCommand.addReturnValue("Integer reply: the length of the list after the push operation");
        rpushCommand.addExample("RPUSH mylist \"hello\"");
        rpushCommand.addExample("RPUSH mylist \"world\"");
        
        // LPOP command
        RedisCommand lpopCommand = new RedisCommand(
                "lpop",
                "LPOP key [count]",
                "Removes and returns the first element of a list"
        );
        lpopCommand.setComplexity(RedisCommandComplexity.O1);
        lpopCommand.setSince("1.0.0");
        lpopCommand.addParameter("key - The key of the list");
        lpopCommand.addParameter("count - The number of elements to pop (optional, since Redis 6.2)");
        lpopCommand.addReturnValue("Bulk string reply: the value of the first element, or nil when key does not exist");
        lpopCommand.addReturnValue("Array reply: the values of the first count elements, or nil when key does not exist (Redis 6.2)");
        lpopCommand.addExample("LPOP mylist");
        lpopCommand.addExample("LPOP mylist 2");
        
        // RPOP command
        RedisCommand rpopCommand = new RedisCommand(
                "rpop",
                "RPOP key [count]",
                "Removes and returns the last element of a list"
        );
        rpopCommand.setComplexity(RedisCommandComplexity.O1);
        rpopCommand.setSince("1.0.0");
        rpopCommand.addParameter("key - The key of the list");
        rpopCommand.addParameter("count - The number of elements to pop (optional, since Redis 6.2)");
        rpopCommand.addReturnValue("Bulk string reply: the value of the last element, or nil when key does not exist");
        rpopCommand.addReturnValue("Array reply: the values of the last count elements, or nil when key does not exist (Redis 6.2)");
        rpopCommand.addExample("RPOP mylist");
        rpopCommand.addExample("RPOP mylist 2");
        
        // LLEN command
        RedisCommand llenCommand = new RedisCommand(
                "llen",
                "LLEN key",
                "Returns the length of a list"
        );
        llenCommand.setComplexity(RedisCommandComplexity.O1);
        llenCommand.setSince("1.0.0");
        llenCommand.addParameter("key - The key of the list");
        llenCommand.addReturnValue("Integer reply: the length of the list at key");
        llenCommand.addExample("LLEN mylist");
        
        // LRANGE command
        RedisCommand lrangeCommand = new RedisCommand(
                "lrange",
                "LRANGE key start stop",
                "Returns a range of elements from a list"
        );
        lrangeCommand.setComplexity(RedisCommandComplexity.ON);
        lrangeCommand.setSince("1.0.0");
        lrangeCommand.addParameter("key - The key of the list");
        lrangeCommand.addParameter("start - The starting index (0-based, can be negative)");
        lrangeCommand.addParameter("stop - The ending index (inclusive, can be negative)");
        lrangeCommand.addReturnValue("Array reply: list of elements in the specified range");
        lrangeCommand.addExample("LRANGE mylist 0 -1");
        lrangeCommand.addExample("LRANGE mylist 0 2");
        
        // LINDEX command
        RedisCommand lindexCommand = new RedisCommand(
                "lindex",
                "LINDEX key index",
                "Returns an element from a list by its index"
        );
        lindexCommand.setComplexity(RedisCommandComplexity.ON);
        lindexCommand.setSince("1.0.0");
        lindexCommand.addParameter("key - The key of the list");
        lindexCommand.addParameter("index - The index of the element to return (0-based, can be negative)");
        lindexCommand.addReturnValue("Bulk string reply: the requested element, or nil when index is out of range");
        lindexCommand.addExample("LINDEX mylist 0");
        lindexCommand.addExample("LINDEX mylist -1");
        
        // LSET command
        RedisCommand lsetCommand = new RedisCommand(
                "lset",
                "LSET key index element",
                "Sets the value of an element in a list by its index"
        );
        lsetCommand.setComplexity(RedisCommandComplexity.ON);
        lsetCommand.setSince("1.0.0");
        lsetCommand.addParameter("key - The key of the list");
        lsetCommand.addParameter("index - The index of the element to set (0-based, can be negative)");
        lsetCommand.addParameter("element - The new value for the element");
        lsetCommand.addReturnValue("Simple string reply: OK if the index exists");
        lsetCommand.addExample("LSET mylist 0 \"Hello\"");
        lsetCommand.addExample("LSET mylist -1 \"World\"");
        
        // Register commands
        category.addCommand(lpushCommand);
        category.addCommand(rpushCommand);
        category.addCommand(lpopCommand);
        category.addCommand(rpopCommand);
        category.addCommand(llenCommand);
        category.addCommand(lrangeCommand);
        category.addCommand(lindexCommand);
        category.addCommand(lsetCommand);
        
        // Add to command map
        commandsByName.put(lpushCommand.getName(), lpushCommand);
        commandsByName.put(rpushCommand.getName(), rpushCommand);
        commandsByName.put(lpopCommand.getName(), lpopCommand);
        commandsByName.put(rpopCommand.getName(), rpopCommand);
        commandsByName.put(llenCommand.getName(), llenCommand);
        commandsByName.put(lrangeCommand.getName(), lrangeCommand);
        commandsByName.put(lsetCommand.getName(), lsetCommand);
    }
    
    private void addHashCommands(RedisCommandCategory category) {
        // HSET command
        RedisCommand hsetCommand = new RedisCommand(
                "hset",
                "HSET key field value [field value ...]",
                "Sets the value of one or more fields in a hash"
        );
        hsetCommand.setComplexity(RedisCommandComplexity.O1);
        hsetCommand.setSince("2.0.0");
        hsetCommand.addParameter("key - The key of the hash");
        hsetCommand.addParameter("field - The field to set");
        hsetCommand.addParameter("value - The value to set for the field");
        hsetCommand.addParameter("field value ... - Additional field-value pairs to set");
        hsetCommand.addReturnValue("Integer reply: the number of fields that were added (not updated)");
        hsetCommand.addExample("HSET myhash field1 \"Hello\"");
        hsetCommand.addExample("HSET myhash field1 \"Hello\" field2 \"World\"");
        
        // HSETNX command
        RedisCommand hsetnxCommand = new RedisCommand(
                "hsetnx",
                "HSETNX key field value",
                "Sets the value of a field in a hash only if the field does not exist"
        );
        hsetnxCommand.setComplexity(RedisCommandComplexity.O1);
        hsetnxCommand.setSince("2.0.0");
        hsetnxCommand.addParameter("key - The key of the hash");
        hsetnxCommand.addParameter("field - The field to set");
        hsetnxCommand.addParameter("value - The value to set for the field");
        hsetnxCommand.addReturnValue("Integer reply: 1 if the field was set, 0 if the field already exists");
        hsetnxCommand.addExample("HSETNX myhash field1 \"Hello\"");
        
        // HGET command
        RedisCommand hgetCommand = new RedisCommand(
                "hget",
                "HGET key field",
                "Returns the value of a field in a hash"
        );
        hgetCommand.setComplexity(RedisCommandComplexity.O1);
        hgetCommand.setSince("2.0.0");
        hgetCommand.addParameter("key - The key of the hash");
        hgetCommand.addParameter("field - The field to get");
        hgetCommand.addReturnValue("Bulk string reply: the value of the field, or nil if the field or key does not exist");
        hgetCommand.addExample("HGET myhash field1");
        
        // HMGET command
        RedisCommand hmgetCommand = new RedisCommand(
                "hmget",
                "HMGET key field [field ...]",
                "Returns the values of multiple fields in a hash"
        );
        hmgetCommand.setComplexity(RedisCommandComplexity.O1);
        hmgetCommand.setSince("2.0.0");
        hmgetCommand.addParameter("key - The key of the hash");
        hmgetCommand.addParameter("field - The first field to get");
        hmgetCommand.addParameter("field ... - Additional fields to get");
        hmgetCommand.addReturnValue("Array reply: list of values at the specified fields");
        hmgetCommand.addExample("HMGET myhash field1 field2 field3");
        
        // HDEL command
        RedisCommand hdelCommand = new RedisCommand(
                "hdel",
                "HDEL key field [field ...]",
                "Deletes one or more fields from a hash"
        );
        hdelCommand.setComplexity(RedisCommandComplexity.O1);
        hdelCommand.setSince("2.0.0");
        hdelCommand.addParameter("key - The key of the hash");
        hdelCommand.addParameter("field - The first field to delete");
        hdelCommand.addParameter("field ... - Additional fields to delete");
        hdelCommand.addReturnValue("Integer reply: the number of fields that were removed");
        hdelCommand.addExample("HDEL myhash field1");
        hdelCommand.addExample("HDEL myhash field1 field2");
        
        // HGETALL command
        RedisCommand hgetallCommand = new RedisCommand(
                "hgetall",
                "HGETALL key",
                "Returns all fields and values in a hash"
        );
        hgetallCommand.setComplexity(RedisCommandComplexity.ON);
        hgetallCommand.setSince("2.0.0");
        hgetallCommand.addParameter("key - The key of the hash");
        hgetallCommand.addReturnValue("Array reply: list of fields and their values, alternating field and value");
        hgetallCommand.addExample("HGETALL myhash");
        
        // HKEYS command
        RedisCommand hkeysCommand = new RedisCommand(
                "hkeys",
                "HKEYS key",
                "Returns all fields in a hash"
        );
        hkeysCommand.setComplexity(RedisCommandComplexity.ON);
        hkeysCommand.setSince("2.0.0");
        hkeysCommand.addParameter("key - The key of the hash");
        hkeysCommand.addReturnValue("Array reply: list of fields in the hash");
        hkeysCommand.addExample("HKEYS myhash");
        
        // HVALS command
        RedisCommand hvalsCommand = new RedisCommand(
                "hvals",
                "HVALS key",
                "Returns all values in a hash"
        );
        hvalsCommand.setComplexity(RedisCommandComplexity.ON);
        hvalsCommand.setSince("2.0.0");
        hvalsCommand.addParameter("key - The key of the hash");
        hvalsCommand.addReturnValue("Array reply: list of values in the hash");
        hvalsCommand.addExample("HVALS myhash");
        
        // HINCRBY command
        RedisCommand hincrbyCommand = new RedisCommand(
                "hincrby",
                "HINCRBY key field increment",
                "Increments the integer value of a field in a hash by the given number"
        );
        hincrbyCommand.setComplexity(RedisCommandComplexity.O1);
        hincrbyCommand.setSince("2.0.0");
        hincrbyCommand.addParameter("key - The key of the hash");
        hincrbyCommand.addParameter("field - The field to increment");
        hincrbyCommand.addParameter("increment - The amount to increment by");
        hincrbyCommand.addReturnValue("Integer reply: the value of the field after the increment");
        hincrbyCommand.addExample("HINCRBY myhash field1 1");
        hincrbyCommand.addExample("HINCRBY myhash field1 -5");
        
        // HEXISTS command
        RedisCommand hexistsCommand = new RedisCommand(
                "hexists",
                "HEXISTS key field",
                "Checks if a field exists in a hash"
        );
        hexistsCommand.setComplexity(RedisCommandComplexity.O1);
        hexistsCommand.setSince("2.0.0");
        hexistsCommand.addParameter("key - The key of the hash");
        hexistsCommand.addParameter("field - The field to check");
        hexistsCommand.addReturnValue("Integer reply: 1 if the field exists, 0 if not");
        hexistsCommand.addExample("HEXISTS myhash field1");
        
        // HLEN command
        RedisCommand hlenCommand = new RedisCommand(
                "hlen",
                "HLEN key",
                "Returns the number of fields in a hash"
        );
        hlenCommand.setComplexity(RedisCommandComplexity.O1);
        hlenCommand.setSince("2.0.0");
        hlenCommand.addParameter("key - The key of the hash");
        hlenCommand.addReturnValue("Integer reply: the number of fields in the hash");
        hlenCommand.addExample("HLEN myhash");
        
        // Register commands
        category.addCommand(hsetCommand);
        category.addCommand(hsetnxCommand);
        category.addCommand(hgetCommand);
        category.addCommand(hmgetCommand);
        category.addCommand(hdelCommand);
        category.addCommand(hgetallCommand);
        category.addCommand(hkeysCommand);
        category.addCommand(hvalsCommand);
        category.addCommand(hincrbyCommand);
        category.addCommand(hexistsCommand);
        category.addCommand(hlenCommand);
        
        // Add to command map
        commandsByName.put(hsetCommand.getName(), hsetCommand);
        commandsByName.put(hsetnxCommand.getName(), hsetnxCommand);
        commandsByName.put(hgetCommand.getName(), hgetCommand);
        commandsByName.put(hmgetCommand.getName(), hmgetCommand);
        commandsByName.put(hdelCommand.getName(), hdelCommand);
        commandsByName.put(hgetallCommand.getName(), hgetallCommand);
        commandsByName.put(hkeysCommand.getName(), hkeysCommand);
        commandsByName.put(hvalsCommand.getName(), hvalsCommand);
        commandsByName.put(hincrbyCommand.getName(), hincrbyCommand);
        commandsByName.put(hexistsCommand.getName(), hexistsCommand);
        commandsByName.put(hlenCommand.getName(), hlenCommand);
    }
    
    private void addSetCommands(RedisCommandCategory category) {
        // SADD command
        RedisCommand saddCommand = new RedisCommand(
                "sadd",
                "SADD key member [member ...]",
                "Adds one or more members to a set"
        );
        saddCommand.setComplexity(RedisCommandComplexity.O1);
        saddCommand.setSince("1.0.0");
        saddCommand.addParameter("key - The key of the set");
        saddCommand.addParameter("member - The first member to add");
        saddCommand.addParameter("member ... - Additional members to add");
        saddCommand.addReturnValue("Integer reply: the number of members that were added to the set");
        saddCommand.addExample("SADD myset \"Hello\"");
        saddCommand.addExample("SADD myset \"Hello\" \"World\" \"Redis\"");
        
        // SREM command
        RedisCommand sremCommand = new RedisCommand(
                "srem",
                "SREM key member [member ...]",
                "Removes one or more members from a set"
        );
        sremCommand.setComplexity(RedisCommandComplexity.O1);
        sremCommand.setSince("1.0.0");
        sremCommand.addParameter("key - The key of the set");
        sremCommand.addParameter("member - The first member to remove");
        sremCommand.addParameter("member ... - Additional members to remove");
        sremCommand.addReturnValue("Integer reply: the number of members that were removed from the set");
        sremCommand.addExample("SREM myset \"Hello\"");
        sremCommand.addExample("SREM myset \"Hello\" \"World\"");
        
        // SMEMBERS command
        RedisCommand smembersCommand = new RedisCommand(
                "smembers",
                "SMEMBERS key",
                "Returns all members of a set"
        );
        smembersCommand.setComplexity(RedisCommandComplexity.ON);
        smembersCommand.setSince("1.0.0");
        smembersCommand.addParameter("key - The key of the set");
        smembersCommand.addReturnValue("Array reply: all members of the set");
        smembersCommand.addExample("SMEMBERS myset");
        
        // SISMEMBER command
        RedisCommand sismemberCommand = new RedisCommand(
                "sismember",
                "SISMEMBER key member",
                "Checks if a member exists in a set"
        );
        sismemberCommand.setComplexity(RedisCommandComplexity.O1);
        sismemberCommand.setSince("1.0.0");
        sismemberCommand.addParameter("key - The key of the set");
        sismemberCommand.addParameter("member - The member to check for");
        sismemberCommand.addReturnValue("Integer reply: 1 if the member exists, 0 if not");
        sismemberCommand.addExample("SISMEMBER myset \"Hello\"");
        
        // SCARD command
        RedisCommand scardCommand = new RedisCommand(
                "scard",
                "SCARD key",
                "Returns the number of members in a set (cardinality)"
        );
        scardCommand.setComplexity(RedisCommandComplexity.O1);
        scardCommand.setSince("1.0.0");
        scardCommand.addParameter("key - The key of the set");
        scardCommand.addReturnValue("Integer reply: the cardinality (number of elements) of the set");
        scardCommand.addExample("SCARD myset");
        
        // SINTER command
        RedisCommand sinterCommand = new RedisCommand(
                "sinter",
                "SINTER key [key ...]",
                "Returns the intersection of multiple sets"
        );
        sinterCommand.setComplexity(RedisCommandComplexity.ON_SQUARE);
        sinterCommand.setSince("1.0.0");
        sinterCommand.addParameter("key - The first set");
        sinterCommand.addParameter("key ... - Additional sets");
        sinterCommand.addReturnValue("Array reply: members of the intersection");
        sinterCommand.addExample("SINTER set1 set2");
        
        // SUNION command
        RedisCommand sunionCommand = new RedisCommand(
                "sunion",
                "SUNION key [key ...]",
                "Returns the union of multiple sets"
        );
        sunionCommand.setComplexity(RedisCommandComplexity.ON);
        sunionCommand.setSince("1.0.0");
        sunionCommand.addParameter("key - The first set");
        sunionCommand.addParameter("key ... - Additional sets");
        sunionCommand.addReturnValue("Array reply: members of the union");
        sunionCommand.addExample("SUNION set1 set2");
        
        // SDIFF command
        RedisCommand sdiffCommand = new RedisCommand(
                "sdiff",
                "SDIFF key [key ...]",
                "Returns the difference between multiple sets"
        );
        sdiffCommand.setComplexity(RedisCommandComplexity.ON);
        sdiffCommand.setSince("1.0.0");
        sdiffCommand.addParameter("key - The first set");
        sdiffCommand.addParameter("key ... - Additional sets");
        sdiffCommand.addReturnValue("Array reply: members of the difference (members from the first set not in the others)");
        sdiffCommand.addExample("SDIFF set1 set2");
        
        // SPOP command
        RedisCommand spopCommand = new RedisCommand(
                "spop",
                "SPOP key [count]",
                "Removes and returns one or more random members from a set");
        spopCommand.setComplexity(RedisCommandComplexity.O1);
        spopCommand.setSince("1.0.0");
        spopCommand.addParameter("key - The key of the set");
        spopCommand.addParameter("count - The number of members to pop (optional, since Redis 3.2)");
        spopCommand.addReturnValue("Bulk string reply: the removed member, or nil if the set is empty");
        spopCommand.addReturnValue("Array reply: the removed members, or empty array if the set is empty (Redis 3.2)");
        spopCommand.addExample("SPOP myset");
        spopCommand.addExample("SPOP myset 3");
        
        // SRANDMEMBER command
        RedisCommand srandmemberCommand = new RedisCommand(
                "srandmember",
                "SRANDMEMBER key [count]",
                "Returns one or multiple random members from a set"
        );
        srandmemberCommand.setComplexity(RedisCommandComplexity.O1);
        srandmemberCommand.setSince("1.0.0");
        srandmemberCommand.addParameter("key - The key of the set");
        srandmemberCommand.addParameter("count - The number of members to return (optional, since Redis 2.6)");
        srandmemberCommand.addReturnValue("Bulk string reply: the randomly selected member, or nil if the set is empty");
        srandmemberCommand.addReturnValue("Array reply: the randomly selected members, or empty array if the set is empty (Redis 2.6)");
        srandmemberCommand.addExample("SRANDMEMBER myset");
        srandmemberCommand.addExample("SRANDMEMBER myset 2");
        
        // SMOVE command
        RedisCommand smoveCommand = new RedisCommand(
                "smove",
                "SMOVE source destination member",
                "Moves a member from one set to another"
        );
        smoveCommand.setComplexity(RedisCommandComplexity.O1);
        smoveCommand.setSince("1.0.0");
        smoveCommand.addParameter("source - The key of the source set");
        smoveCommand.addParameter("destination - The key of the destination set");
        smoveCommand.addParameter("member - The member to move");
        smoveCommand.addReturnValue("Integer reply: 1 if the element was moved, 0 if it was not found in the source set");
        smoveCommand.addExample("SMOVE myset otherset \"Hello\"");
        
        // SINTERSTORE command
        RedisCommand sinterstoreCommand = new RedisCommand(
                "sinterstore",
                "SINTERSTORE destination key [key ...]",
                "Stores the intersection of multiple sets in a key"
        );
        sinterstoreCommand.setComplexity(RedisCommandComplexity.ON_SQUARE);
        sinterstoreCommand.setSince("1.0.0");
        sinterstoreCommand.addParameter("destination - The key to store the result in");
        sinterstoreCommand.addParameter("key - The first set");
        sinterstoreCommand.addParameter("key ... - Additional sets");
        sinterstoreCommand.addReturnValue("Integer reply: the number of elements in the resulting set");
        sinterstoreCommand.addExample("SINTERSTORE result set1 set2");
        
        // SUNIONSTORE command
        RedisCommand sunionstoreCommand = new RedisCommand(
                "sunionstore",
                "SUNIONSTORE destination key [key ...]",
                "Stores the union of multiple sets in a key"
        );
        sunionstoreCommand.setComplexity(RedisCommandComplexity.ON);
        sunionstoreCommand.setSince("1.0.0");
        sunionstoreCommand.addParameter("destination - The key to store the result in");
        sunionstoreCommand.addParameter("key - The first set");
        sunionstoreCommand.addParameter("key ... - Additional sets");
        sunionstoreCommand.addReturnValue("Integer reply: the number of elements in the resulting set");
        sunionstoreCommand.addExample("SUNIONSTORE result set1 set2");
        
        // SDIFFSTORE command
        RedisCommand sdiffstoreCommand = new RedisCommand(
                "sdiffstore",
                "SDIFFSTORE destination key [key ...]",
                "Stores the difference between multiple sets in a key"
        );
        sdiffstoreCommand.setComplexity(RedisCommandComplexity.ON);
        sdiffstoreCommand.setSince("1.0.0");
        sdiffstoreCommand.addParameter("destination - The key to store the result in");
        sdiffstoreCommand.addParameter("key - The first set");
        sdiffstoreCommand.addParameter("key ... - Additional sets");
        sdiffstoreCommand.addReturnValue("Integer reply: the number of elements in the resulting set");
        sdiffstoreCommand.addExample("SDIFFSTORE result set1 set2");
        
        // Register commands
        category.addCommand(saddCommand);
        category.addCommand(sremCommand);
        category.addCommand(smembersCommand);
        category.addCommand(sismemberCommand);
        category.addCommand(scardCommand);
        category.addCommand(sinterCommand);
        category.addCommand(sunionCommand);
        category.addCommand(sdiffCommand);
        category.addCommand(spopCommand);
        category.addCommand(srandmemberCommand);
        category.addCommand(smoveCommand);
        category.addCommand(sinterstoreCommand);
        category.addCommand(sunionstoreCommand);
        category.addCommand(sdiffstoreCommand);
        
        // Add to command map
        commandsByName.put(saddCommand.getName(), saddCommand);
        commandsByName.put(sremCommand.getName(), sremCommand);
        commandsByName.put(smembersCommand.getName(), smembersCommand);
        commandsByName.put(sismemberCommand.getName(), sismemberCommand);
        commandsByName.put(scardCommand.getName(), scardCommand);
        commandsByName.put(sinterCommand.getName(), sinterCommand);
        commandsByName.put(sunionCommand.getName(), sunionCommand);
        commandsByName.put(sdiffCommand.getName(), sdiffCommand);
        commandsByName.put(spopCommand.getName(), spopCommand);
        commandsByName.put(srandmemberCommand.getName(), srandmemberCommand);
        commandsByName.put(smoveCommand.getName(), smoveCommand);
        commandsByName.put(sinterstoreCommand.getName(), sinterstoreCommand);
        commandsByName.put(sunionstoreCommand.getName(), sunionstoreCommand);
        commandsByName.put(sdiffstoreCommand.getName(), sdiffstoreCommand);
    }
    
    private void addSortedSetCommands(RedisCommandCategory category) {
        // ZADD command
        RedisCommand zaddCommand = new RedisCommand(
                "zadd",
                "ZADD key [NX|XX] [CH] [INCR] score member [score member ...]",
                "Adds one or more members to a sorted set, or updates their score if they already exist"
        );
        zaddCommand.setComplexity(RedisCommandComplexity.OLOG_N);
        zaddCommand.setSince("1.2.0");
        zaddCommand.addParameter("key - The key of the sorted set");
        zaddCommand.addParameter("NX - Only add new members, don't update existing members");
        zaddCommand.addParameter("XX - Only update existing members, don't add new members");
        zaddCommand.addParameter("CH - Modify the return value to be the number of changed elements");
        zaddCommand.addParameter("INCR - Increment the score by the specified amount");
        zaddCommand.addParameter("score - The score to assign to the member");
        zaddCommand.addParameter("member - The member to add or update");
        zaddCommand.addParameter("score member ... - Additional score-member pairs");
        zaddCommand.addReturnValue("Integer reply: the number of elements added to the sorted set");
        zaddCommand.addExample("ZADD myzset 1 \"one\"");
        zaddCommand.addExample("ZADD myzset 1 \"one\" 2 \"two\" 3 \"three\"");
        zaddCommand.addExample("ZADD myzset XX CH 1 \"one\" 2 \"two\"");
        
        // ZREM command
        RedisCommand zremCommand = new RedisCommand(
                "zrem",
                "ZREM key member [member ...]",
                "Removes one or more members from a sorted set"
        );
        zremCommand.setComplexity(RedisCommandComplexity.OLOG_N);
        zremCommand.setSince("1.2.0");
        zremCommand.addParameter("key - The key of the sorted set");
        zremCommand.addParameter("member - The first member to remove");
        zremCommand.addParameter("member ... - Additional members to remove");
        zremCommand.addReturnValue("Integer reply: the number of members that were removed");
        zremCommand.addExample("ZREM myzset \"one\"");
        zremCommand.addExample("ZREM myzset \"one\" \"two\"");
        
        // ZRANGE command
        RedisCommand zrangeCommand = new RedisCommand(
                "zrange",
                "ZRANGE key start stop [WITHSCORES]",
                "Returns a range of members in a sorted set, by index"
        );
        zrangeCommand.setComplexity(RedisCommandComplexity.OLOG_N);
        zrangeCommand.setSince("1.2.0");
        zrangeCommand.addParameter("key - The key of the sorted set");
        zrangeCommand.addParameter("start - The starting index (0-based, can be negative)");
        zrangeCommand.addParameter("stop - The ending index (inclusive, can be negative)");
        zrangeCommand.addParameter("WITHSCORES - Also return the scores of the elements");
        zrangeCommand.addReturnValue("Array reply: list of members in the specified range");
        zrangeCommand.addExample("ZRANGE myzset 0 -1");
        zrangeCommand.addExample("ZRANGE myzset 0 -1 WITHSCORES");
        
        // ZREVRANGE command
        RedisCommand zrevrangeCommand = new RedisCommand(
                "zrevrange",
                "ZREVRANGE key start stop [WITHSCORES]",
                "Returns a range of members in a sorted set, by index, with scores ordered from high to low"
        );
        zrevrangeCommand.setComplexity(RedisCommandComplexity.OLOG_N);
        zrevrangeCommand.setSince("1.2.0");
        zrevrangeCommand.addParameter("key - The key of the sorted set");
        zrevrangeCommand.addParameter("start - The starting index (0-based, can be negative)");
        zrevrangeCommand.addParameter("stop - The ending index (inclusive, can be negative)");
        zrevrangeCommand.addParameter("WITHSCORES - Also return the scores of the elements");
        zrevrangeCommand.addReturnValue("Array reply: list of members in the specified range, from high to low score");
        zrevrangeCommand.addExample("ZREVRANGE myzset 0 -1");
        zrevrangeCommand.addExample("ZREVRANGE myzset 0 -1 WITHSCORES");
        
        // ZRANK command
        RedisCommand zrankCommand = new RedisCommand(
                "zrank",
                "ZRANK key member",
                "Returns the rank of a member in a sorted set, from low to high"
        );
        zrankCommand.setComplexity(RedisCommandComplexity.OLOG_N);
        zrankCommand.setSince("2.0.0");
        zrankCommand.addParameter("key - The key of the sorted set");
        zrankCommand.addParameter("member - The member to get the rank for");
        zrankCommand.addReturnValue("Integer reply: the rank of the member, or nil if the member or key does not exist");
        zrankCommand.addExample("ZRANK myzset \"one\"");
        
        // ZREVRANK command
        RedisCommand zrevrankCommand = new RedisCommand(
                "zrevrank",
                "ZREVRANK key member",
                "Returns the rank of a member in a sorted set, from high to low"
        );
        zrevrankCommand.setComplexity(RedisCommandComplexity.OLOG_N);
        zrevrankCommand.setSince("2.0.0");
        zrevrankCommand.addParameter("key - The key of the sorted set");
        zrevrankCommand.addParameter("member - The member to get the rank for");
        zrevrankCommand.addReturnValue("Integer reply: the rank of the member, or nil if the member or key does not exist");
        zrevrankCommand.addExample("ZREVRANK myzset \"one\"");
        
        // ZSCORE command
        RedisCommand zscoreCommand = new RedisCommand(
                "zscore",
                "ZSCORE key member",
                "Returns the score of a member in a sorted set"
        );
        zscoreCommand.setComplexity(RedisCommandComplexity.O1);
        zscoreCommand.setSince("1.2.0");
        zscoreCommand.addParameter("key - The key of the sorted set");
        zscoreCommand.addParameter("member - The member to get the score for");
        zscoreCommand.addReturnValue("Bulk string reply: the score of the member (a double value), or nil if the member or key does not exist");
        zscoreCommand.addExample("ZSCORE myzset \"one\"");
        
        // ZCARD command
        RedisCommand zcardCommand = new RedisCommand(
                "zcard",
                "ZCARD key",
                "Returns the cardinality (number of elements) of a sorted set"
        );
        zcardCommand.setComplexity(RedisCommandComplexity.O1);
        zcardCommand.setSince("1.2.0");
        zcardCommand.addParameter("key - The key of the sorted set");
        zcardCommand.addReturnValue("Integer reply: the cardinality of the sorted set, or 0 if the key does not exist");
        zcardCommand.addExample("ZCARD myzset");
        
        // ZCOUNT command
        RedisCommand zcountCommand = new RedisCommand(
                "zcount",
                "ZCOUNT key min max",
                "Returns the number of members in a sorted set with scores within the given range"
        );
        zcountCommand.setComplexity(RedisCommandComplexity.OLOG_N);
        zcountCommand.setSince("2.0.0");
        zcountCommand.addParameter("key - The key of the sorted set");
        zcountCommand.addParameter("min - The minimum score (inclusive, can be -inf)");
        zcountCommand.addParameter("max - The maximum score (inclusive, can be +inf)");
        zcountCommand.addReturnValue("Integer reply: the number of members in the specified score range");
        zcountCommand.addExample("ZCOUNT myzset 1 3");
        zcountCommand.addExample("ZCOUNT myzset -inf +inf");
        
        // ZINCRBY command
        RedisCommand zincrbyCommand = new RedisCommand(
                "zincrby",
                "ZINCRBY key increment member",
                "Increments the score of a member in a sorted set"
        );
        zincrbyCommand.setComplexity(RedisCommandComplexity.OLOG_N);
        zincrbyCommand.setSince("1.2.0");
        zincrbyCommand.addParameter("key - The key of the sorted set");
        zincrbyCommand.addParameter("increment - The amount to increment by (can be negative)");
        zincrbyCommand.addParameter("member - The member to increment the score for");
        zincrbyCommand.addReturnValue("Bulk string reply: the new score of the member (a double precision value)");
        zincrbyCommand.addExample("ZINCRBY myzset 2 \"one\"");
        zincrbyCommand.addExample("ZINCRBY myzset -1 \"two\"");
        
        // ZRANGEBYSCORE command
        RedisCommand zrangebyscoreCommand = new RedisCommand(
                "zrangebyscore",
                "ZRANGEBYSCORE key min max [WITHSCORES] [LIMIT offset count]",
                "Returns members in a sorted set with scores within the given range"
        );
        zrangebyscoreCommand.setComplexity(RedisCommandComplexity.OLOG_N);
        zrangebyscoreCommand.setSince("1.0.5");
        zrangebyscoreCommand.addParameter("key - The key of the sorted set");
        zrangebyscoreCommand.addParameter("min - The minimum score (inclusive, can be -inf)");
        zrangebyscoreCommand.addParameter("max - The maximum score (inclusive, can be +inf)");
        zrangebyscoreCommand.addParameter("WITHSCORES - Also return the scores of the elements");
        zrangebyscoreCommand.addParameter("LIMIT offset count - Limit the results to offset, count elements");
        zrangebyscoreCommand.addReturnValue("Array reply: list of members in the specified score range");
        zrangebyscoreCommand.addExample("ZRANGEBYSCORE myzset 1 3");
        zrangebyscoreCommand.addExample("ZRANGEBYSCORE myzset -inf +inf WITHSCORES");
        zrangebyscoreCommand.addExample("ZRANGEBYSCORE myzset -inf +inf LIMIT 1 2");
        
        // ZREVRANGEBYSCORE command
        RedisCommand zrevrangebyscoreCommand = new RedisCommand(
                "zrevrangebyscore",
                "ZREVRANGEBYSCORE key max min [WITHSCORES] [LIMIT offset count]",
                "Returns members in a sorted set with scores within the given range, ordered from high to low"
        );
        zrevrangebyscoreCommand.setComplexity(RedisCommandComplexity.OLOG_N);
        zrevrangebyscoreCommand.setSince("2.2.0");
        zrevrangebyscoreCommand.addParameter("key - The key of the sorted set");
        zrevrangebyscoreCommand.addParameter("max - The maximum score (inclusive, can be +inf)");
        zrevrangebyscoreCommand.addParameter("min - The minimum score (inclusive, can be -inf)");
        zrevrangebyscoreCommand.addParameter("WITHSCORES - Also return the scores of the elements");
        zrevrangebyscoreCommand.addParameter("LIMIT offset count - Limit the results to offset, count elements");
        zrevrangebyscoreCommand.addReturnValue("Array reply: list of members in the specified score range, ordered from high to low");
        zrevrangebyscoreCommand.addExample("ZREVRANGEBYSCORE myzset 3 1");
        zrevrangebyscoreCommand.addExample("ZREVRANGEBYSCORE myzset +inf -inf WITHSCORES");
        
        // ZREMRANGEBYRANK command
        RedisCommand zremrangebyrankCommand = new RedisCommand(
                "zremrangebyrank",
                "ZREMRANGEBYRANK key start stop",
                "Removes members in a sorted set within the given indexes"
        );
        zremrangebyrankCommand.setComplexity(RedisCommandComplexity.OLOG_N);
        zremrangebyrankCommand.setSince("2.0.0");
        zremrangebyrankCommand.addParameter("key - The key of the sorted set");
        zremrangebyrankCommand.addParameter("start - The starting index (0-based, can be negative)");
        zremrangebyrankCommand.addParameter("stop - The ending index (inclusive, can be negative)");
        zremrangebyrankCommand.addReturnValue("Integer reply: the number of members removed");
        zremrangebyrankCommand.addExample("ZREMRANGEBYRANK myzset 0 1");
        zremrangebyrankCommand.addExample("ZREMRANGEBYRANK myzset 0 -1");
        
        // ZREMRANGEBYSCORE command
        RedisCommand zremrangebyscoreCommand = new RedisCommand(
                "zremrangebyscore",
                "ZREMRANGEBYSCORE key min max",
                "Removes members in a sorted set within the given scores"
        );
        zremrangebyscoreCommand.setComplexity(RedisCommandComplexity.OLOG_N);
        zremrangebyscoreCommand.setSince("1.2.0");
        zremrangebyscoreCommand.addParameter("key - The key of the sorted set");
        zremrangebyscoreCommand.addParameter("min - The minimum score (inclusive, can be -inf)");
        zremrangebyscoreCommand.addParameter("max - The maximum score (inclusive, can be +inf)");
        zremrangebyscoreCommand.addReturnValue("Integer reply: the number of members removed");
        zremrangebyscoreCommand.addExample("ZREMRANGEBYSCORE myzset 1 2");
        zremrangebyscoreCommand.addExample("ZREMRANGEBYSCORE myzset -inf +inf");
        
        // Register commands
        category.addCommand(zaddCommand);
        category.addCommand(zremCommand);
        category.addCommand(zrangeCommand);
        category.addCommand(zrevrangeCommand);
        category.addCommand(zrankCommand);
        category.addCommand(zrevrankCommand);
        category.addCommand(zscoreCommand);
        category.addCommand(zcardCommand);
        category.addCommand(zcountCommand);
        category.addCommand(zincrbyCommand);
        category.addCommand(zrangebyscoreCommand);
        category.addCommand(zrevrangebyscoreCommand);
        category.addCommand(zremrangebyrankCommand);
        category.addCommand(zremrangebyscoreCommand);
        
        // Add to command map
        commandsByName.put(zaddCommand.getName(), zaddCommand);
        commandsByName.put(zremCommand.getName(), zremCommand);
        commandsByName.put(zrangeCommand.getName(), zrangeCommand);
        commandsByName.put(zrevrangeCommand.getName(), zrevrangeCommand);
        commandsByName.put(zrankCommand.getName(), zrankCommand);
        commandsByName.put(zrevrankCommand.getName(), zrevrankCommand);
        commandsByName.put(zscoreCommand.getName(), zscoreCommand);
        commandsByName.put(zcardCommand.getName(), zcardCommand);
        commandsByName.put(zcountCommand.getName(), zcountCommand);
        commandsByName.put(zincrbyCommand.getName(), zincrbyCommand);
        commandsByName.put(zrangebyscoreCommand.getName(), zrangebyscoreCommand);
        commandsByName.put(zrevrangebyscoreCommand.getName(), zrevrangebyscoreCommand);
        commandsByName.put(zremrangebyrankCommand.getName(), zremrangebyrankCommand);
        commandsByName.put(zremrangebyscoreCommand.getName(), zremrangebyscoreCommand);
    }
    
    private void addKeyCommands(RedisCommandCategory category) {
        // DEL command
        RedisCommand delCommand = new RedisCommand(
                "del",
                "DEL key [key ...]",
                "Deletes one or more keys"
        );
        delCommand.setComplexity(RedisCommandComplexity.ON);
        delCommand.setSince("1.0.0");
        delCommand.addParameter("key - The first key to delete");
        delCommand.addParameter("key ... - Additional keys to delete");
        delCommand.addReturnValue("Integer reply: the number of keys that were removed");
        delCommand.addExample("DEL key1 key2 key3");
        
        // UNLINK command
        RedisCommand unlinkCommand = new RedisCommand(
                "unlink",
                "UNLINK key [key ...]",
                "Asynchronously deletes one or more keys"
        );
        unlinkCommand.setComplexity(RedisCommandComplexity.O1);
        unlinkCommand.setSince("4.0.0");
        unlinkCommand.addParameter("key - The first key to unlink");
        unlinkCommand.addParameter("key ... - Additional keys to unlink");
        unlinkCommand.addReturnValue("Integer reply: the number of keys that were unlinked");
        unlinkCommand.addExample("UNLINK key1 key2 key3");
        
        // EXISTS command
        RedisCommand existsCommand = new RedisCommand(
                "exists",
                "EXISTS key [key ...]",
                "Checks if one or more keys exist"
        );
        existsCommand.setComplexity(RedisCommandComplexity.O1);
        existsCommand.setSince("1.0.0");
        existsCommand.addParameter("key - The first key to check");
        existsCommand.addParameter("key ... - Additional keys to check");
        existsCommand.addReturnValue("Integer reply: the number of keys that exist");
        existsCommand.addExample("EXISTS key1");
        existsCommand.addExample("EXISTS key1 key2 nonexisting");
        
        // EXPIRE command
        RedisCommand expireCommand = new RedisCommand(
                "expire",
                "EXPIRE key seconds [NX|XX|GT|LT]",
                "Sets a timeout on a key, after which it will be automatically deleted"
        );
        expireCommand.setComplexity(RedisCommandComplexity.O1);
        expireCommand.setSince("1.0.0");
        expireCommand.addParameter("key - The key to set a timeout on");
        expireCommand.addParameter("seconds - The timeout in seconds");
        expireCommand.addParameter("NX - Set expiry only when the key has no expiry");
        expireCommand.addParameter("XX - Set expiry only when the key has an existing expiry");
        expireCommand.addParameter("GT - Set expiry only when the new expiry is greater than current one");
        expireCommand.addParameter("LT - Set expiry only when the new expiry is less than current one");
        expireCommand.addReturnValue("Integer reply: 1 if the timeout was set, 0 if not (e.g., key doesn't exist)");
        expireCommand.addExample("EXPIRE mykey 10");
        expireCommand.addExample("EXPIRE mykey 10 NX");
        
        // EXPIREAT command
        RedisCommand expireatCommand = new RedisCommand(
                "expireat",
                "EXPIREAT key timestamp [NX|XX|GT|LT]",
                "Sets the expiry for a key as a UNIX timestamp"
        );
        expireatCommand.setComplexity(RedisCommandComplexity.O1);
        expireatCommand.setSince("1.2.0");
        expireatCommand.addParameter("key - The key to set a timeout on");
        expireatCommand.addParameter("timestamp - The timeout as a UNIX timestamp (seconds since January 1, 1970)");
        expireatCommand.addParameter("NX - Set expiry only when the key has no expiry");
        expireatCommand.addParameter("XX - Set expiry only when the key has an existing expiry");
        expireatCommand.addParameter("GT - Set expiry only when the new expiry is greater than current one");
        expireatCommand.addParameter("LT - Set expiry only when the new expiry is less than current one");
        expireatCommand.addReturnValue("Integer reply: 1 if the timeout was set, 0 if not (e.g., key doesn't exist)");
        expireatCommand.addExample("EXPIREAT mykey 1637010189");
        
        // TTL command
        RedisCommand ttlCommand = new RedisCommand(
                "ttl",
                "TTL key",
                "Returns the remaining time to live of a key"
        );
        ttlCommand.setComplexity(RedisCommandComplexity.O1);
        ttlCommand.setSince("1.0.0");
        ttlCommand.addParameter("key - The key to get the TTL for");
        ttlCommand.addReturnValue("Integer reply: TTL in seconds, or -1 if the key has no TTL, or -2 if the key does not exist");
        ttlCommand.addExample("TTL mykey");
        
        // TYPE command
        RedisCommand typeCommand = new RedisCommand(
                "type",
                "TYPE key",
                "Returns the data type of the value stored at a key"
        );
        typeCommand.setComplexity(RedisCommandComplexity.O1);
        typeCommand.setSince("1.0.0");
        typeCommand.addParameter("key - The key to get the type for");
        typeCommand.addReturnValue("Simple string reply: type of the value stored at key (string, list, set, zset, hash, stream) or none if the key does not exist");
        typeCommand.addExample("TYPE mykey");
        
        // RENAME command
        RedisCommand renameCommand = new RedisCommand(
                "rename",
                "RENAME key newkey",
                "Renames a key"
        );
        renameCommand.setComplexity(RedisCommandComplexity.O1);
        renameCommand.setSince("1.0.0");
        renameCommand.addParameter("key - The key to rename");
        renameCommand.addParameter("newkey - The new name for the key");
        renameCommand.addReturnValue("Simple string reply: OK");
        renameCommand.addExample("RENAME mykey mynewkey");
        
        // RENAMENX command
        RedisCommand renamenxCommand = new RedisCommand(
                "renamenx",
                "RENAMENX key newkey",
                "Renames a key, only if the new key does not exist"
        );
        renamenxCommand.setComplexity(RedisCommandComplexity.O1);
        renamenxCommand.addParameter("key - The key to rename");
        renamenxCommand.addParameter("newkey - The new name for the key");
        renamenxCommand.addReturnValue("Integer reply: 1 if the key was renamed, 0 if the new key already exists");
        renamenxCommand.addExample("RENAMENX mykey mynewkey");
        
        // KEYS command
        RedisCommand keysCommand = new RedisCommand(
                "keys",
                "KEYS pattern",
                "Finds all keys matching the specified pattern"
        );
        keysCommand.setComplexity(RedisCommandComplexity.ON);
        keysCommand.setSince("1.0.0");
        keysCommand.addParameter("pattern - Pattern to match (e.g., h?llo, h*llo, h[ae]llo)");
        keysCommand.addReturnValue("Array reply: list of keys matching the pattern");
        keysCommand.addExample("KEYS *");
        keysCommand.addExample("KEYS user:*");
        
        // SCAN command
        RedisCommand scanCommand = new RedisCommand(
                "scan",
                "SCAN cursor [MATCH pattern] [COUNT count] [TYPE type]",
                "Incrementally iterates the keys in a database"
        );
        scanCommand.setComplexity(RedisCommandComplexity.O1);
        scanCommand.setSince("2.8.0");
        scanCommand.addParameter("cursor - The cursor value (start with 0)");
        scanCommand.addParameter("MATCH pattern - Pattern to match (e.g., h?llo, h*llo)");
        scanCommand.addParameter("COUNT count - Number of elements to return per call");
        scanCommand.addParameter("TYPE type - Filter by type (string, list, set, zset, hash, stream)");
        scanCommand.addReturnValue("Array reply: two-element array with cursor for next iteration and array of keys");
        scanCommand.addExample("SCAN 0");
        scanCommand.addExample("SCAN 0 MATCH user:* COUNT 10");
        
        // RANDOMKEY command
        RedisCommand randomkeyCommand = new RedisCommand(
                "randomkey",
                "RANDOMKEY",
                "Returns a random key from the keyspace"
        );
        randomkeyCommand.setComplexity(RedisCommandComplexity.O1);
        randomkeyCommand.setSince("1.0.0");
        randomkeyCommand.addReturnValue("Bulk string reply: a random key, or nil if the database is empty");
        randomkeyCommand.addExample("RANDOMKEY");
        
        // Register commands
        category.addCommand(delCommand);
        category.addCommand(unlinkCommand);
        category.addCommand(existsCommand);
        category.addCommand(expireCommand);
        category.addCommand(expireatCommand);
        category.addCommand(ttlCommand);
        category.addCommand(typeCommand);
        category.addCommand(renameCommand);
        category.addCommand(renamenxCommand);
        category.addCommand(keysCommand);
        category.addCommand(scanCommand);
        category.addCommand(randomkeyCommand);
        
        // Add to command map
        commandsByName.put(delCommand.getName(), delCommand);
        commandsByName.put(unlinkCommand.getName(), unlinkCommand);
        commandsByName.put(existsCommand.getName(), existsCommand);
        commandsByName.put(expireCommand.getName(), expireCommand);
        commandsByName.put(expireatCommand.getName(), expireatCommand);
        commandsByName.put(ttlCommand.getName(), ttlCommand);
        commandsByName.put(typeCommand.getName(), typeCommand);
        commandsByName.put(renameCommand.getName(), renameCommand);
        commandsByName.put(renamenxCommand.getName(), renamenxCommand);
        commandsByName.put(keysCommand.getName(), keysCommand);
        commandsByName.put(scanCommand.getName(), scanCommand);
        commandsByName.put(randomkeyCommand.getName(), randomkeyCommand);
    }
    
    private void addServerCommands(RedisCommandCategory category) {
        // INFO command
        RedisCommand infoCommand = new RedisCommand(
                "info",
                "INFO [section]",
                "Returns information and statistics about the server"
        );
        infoCommand.setComplexity(RedisCommandComplexity.O1);
        infoCommand.setSince("1.0.0");
        infoCommand.addParameter("section - Optional section name (server, clients, memory, persistence, stats, replication, cpu, commandstats, cluster, keyspace)");
        infoCommand.addReturnValue("Bulk string reply: server information");
        infoCommand.addExample("INFO");
        infoCommand.addExample("INFO memory");
        
        // PING command
        RedisCommand pingCommand = new RedisCommand(
                "ping",
                "PING [message]",
                "Tests connection, returns PONG or the provided message"
        );
        pingCommand.setComplexity(RedisCommandComplexity.O1);
        pingCommand.setSince("1.0.0");
        pingCommand.addParameter("message - Optional message to return instead of PONG");
        pingCommand.addReturnValue("Simple string reply: PONG or the message");
        pingCommand.addExample("PING");
        pingCommand.addExample("PING \"hello world\"");
        
        // TIME command
        RedisCommand timeCommand = new RedisCommand(
                "time",
                "TIME",
                "Returns the server time"
        );
        timeCommand.setComplexity(RedisCommandComplexity.O1);
        timeCommand.setSince("2.6.0");
        timeCommand.addReturnValue("Array reply: two-element array with Unix timestamp in seconds and microseconds");
        timeCommand.addExample("TIME");
        
        // CONFIG GET command
        RedisCommand configGetCommand = new RedisCommand(
                "config get",
                "CONFIG GET parameter [parameter ...]",
                "Gets configuration parameters"
        );
        configGetCommand.setComplexity(RedisCommandComplexity.O1);
        configGetCommand.setSince("2.0.0");
        configGetCommand.addParameter("parameter - Parameter name (or pattern)");
        configGetCommand.addParameter("parameter ... - Additional parameters");
        configGetCommand.addReturnValue("Array reply: list of parameter names and values");
        configGetCommand.addExample("CONFIG GET timeout");
        configGetCommand.addExample("CONFIG GET \"*max-*-entries*\"");
        
        // CONFIG SET command
        RedisCommand configSetCommand = new RedisCommand(
                "config set",
                "CONFIG SET parameter value [parameter value ...]",
                "Sets configuration parameters"
        );
        configSetCommand.setComplexity(RedisCommandComplexity.O1);
        configSetCommand.setSince("2.0.0");
        configSetCommand.addParameter("parameter - Parameter name");
        configSetCommand.addParameter("value - Parameter value");
        configSetCommand.addParameter("parameter value ... - Additional parameter-value pairs");
        configSetCommand.addReturnValue("Simple string reply: OK when the configuration was set properly");
        configSetCommand.addExample("CONFIG SET timeout 300");
        
        // DBSIZE command
        RedisCommand dbsizeCommand = new RedisCommand(
                "dbsize",
                "DBSIZE",
                "Returns the number of keys in the current database"
        );
        dbsizeCommand.setComplexity(RedisCommandComplexity.O1);
        dbsizeCommand.setSince("1.0.0");
        dbsizeCommand.addReturnValue("Integer reply: the number of keys in the current database");
        dbsizeCommand.addExample("DBSIZE");
        
        // FLUSHDB command
        RedisCommand flushdbCommand = new RedisCommand(
                "flushdb",
                "FLUSHDB [ASYNC]",
                "Removes all keys from the current database"
        );
        flushdbCommand.setComplexity(RedisCommandComplexity.ON);
        flushdbCommand.setSince("1.0.0");
        flushdbCommand.addParameter("ASYNC - Flush asynchronously (since 4.0.0)");
        flushdbCommand.addReturnValue("Simple string reply: OK");
        flushdbCommand.addExample("FLUSHDB");
        flushdbCommand.addExample("FLUSHDB ASYNC");
        
        // FLUSHALL command
        RedisCommand flushallCommand = new RedisCommand(
                "flushall",
                "FLUSHALL [ASYNC]",
                "Removes all keys from all databases"
        );
        flushallCommand.setComplexity(RedisCommandComplexity.ON);
        flushallCommand.setSince("1.0.0");
        flushallCommand.addParameter("ASYNC - Flush asynchronously (since 4.0.0)");
        flushallCommand.addReturnValue("Simple string reply: OK");
        flushallCommand.addExample("FLUSHALL");
        flushallCommand.addExample("FLUSHALL ASYNC");
        
        // CLIENT LIST command
        RedisCommand clientListCommand = new RedisCommand(
                "client list",
                "CLIENT LIST [TYPE normal|master|replica|pubsub]",
                "Returns information and statistics about client connections"
        );
        clientListCommand.setComplexity(RedisCommandComplexity.ON);
        clientListCommand.setSince("2.4.0");
        clientListCommand.addParameter("TYPE - Filter clients by type");
        clientListCommand.addReturnValue("Bulk string reply: information about client connections");
        clientListCommand.addExample("CLIENT LIST");
        clientListCommand.addExample("CLIENT LIST TYPE pubsub");
        
        // CLIENT KILL command
        RedisCommand clientKillCommand = new RedisCommand(
                "client kill",
                "CLIENT KILL [ip:port] [ID client-id] [TYPE normal|master|replica|pubsub] [ADDR ip:port] [SKIPME yes/no]",
                "Kills a client connection"
        );
        clientKillCommand.setComplexity(RedisCommandComplexity.ON);
        clientKillCommand.setSince("2.4.0");
        clientKillCommand.addParameter("ip:port - Client IP and port");
        clientKillCommand.addParameter("ID client-id - Client ID");
        clientKillCommand.addParameter("TYPE - Kill clients by type");
        clientKillCommand.addParameter("ADDR ip:port - Kill clients by address");
        clientKillCommand.addParameter("SKIPME yes/no - Skip current client (default: yes)");
        clientKillCommand.addReturnValue("Simple string reply: OK when using ip:port");
        clientKillCommand.addReturnValue("Integer reply: the number of clients killed when using other filters");
        clientKillCommand.addExample("CLIENT KILL 127.0.0.1:6379");
        clientKillCommand.addExample("CLIENT KILL ID 12345");
        
        // Register commands
        category.addCommand(infoCommand);
        category.addCommand(pingCommand);
        category.addCommand(timeCommand);
        category.addCommand(configGetCommand);
        category.addCommand(configSetCommand);
        category.addCommand(dbsizeCommand);
        category.addCommand(flushdbCommand);
        category.addCommand(flushallCommand);
        category.addCommand(clientListCommand);
        category.addCommand(clientKillCommand);
        
        // Add to command map
        commandsByName.put(infoCommand.getName(), infoCommand);
        commandsByName.put(pingCommand.getName(), pingCommand);
        commandsByName.put(timeCommand.getName(), timeCommand);
        commandsByName.put(configGetCommand.getName(), configGetCommand);
        commandsByName.put(configSetCommand.getName(), configSetCommand);
        commandsByName.put(dbsizeCommand.getName(), dbsizeCommand);
        commandsByName.put(flushdbCommand.getName(), flushdbCommand);
        commandsByName.put(flushallCommand.getName(), flushallCommand);
        commandsByName.put(clientListCommand.getName(), clientListCommand);
        commandsByName.put(clientKillCommand.getName(), clientKillCommand);
    }
    
    private void addPubSubCommands(RedisCommandCategory category) {
        // PUBLISH command
        RedisCommand publishCommand = new RedisCommand(
                "publish",
                "PUBLISH channel message",
                "Posts a message to a channel"
        );
        publishCommand.setComplexity(RedisCommandComplexity.ON);
        publishCommand.setSince("2.0.0");
        publishCommand.addParameter("channel - The channel to publish to");
        publishCommand.addParameter("message - The message to publish");
        publishCommand.addReturnValue("Integer reply: the number of clients that received the message");
        publishCommand.addExample("PUBLISH news \"Hello World\"");
        
        // SUBSCRIBE command
        RedisCommand subscribeCommand = new RedisCommand(
                "subscribe",
                "SUBSCRIBE channel [channel ...]",
                "Subscribes to channels"
        );
        subscribeCommand.setComplexity(RedisCommandComplexity.O1);
        subscribeCommand.setSince("2.0.0");
        subscribeCommand.addParameter("channel - The first channel to subscribe to");
        subscribeCommand.addParameter("channel ... - Additional channels to subscribe to");
        subscribeCommand.addReturnValue("Array reply: message with subscribe status, channel name, and count of subscribed channels");
        subscribeCommand.addExample("SUBSCRIBE news");
        subscribeCommand.addExample("SUBSCRIBE news chat");
        
        // UNSUBSCRIBE command
        RedisCommand unsubscribeCommand = new RedisCommand(
                "unsubscribe",
                "UNSUBSCRIBE [channel [channel ...]]",
                "Unsubscribes from channels"
        );
        unsubscribeCommand.setComplexity(RedisCommandComplexity.O1);
        unsubscribeCommand.setSince("2.0.0");
        unsubscribeCommand.addParameter("channel - The first channel to unsubscribe from (optional)");
        unsubscribeCommand.addParameter("channel ... - Additional channels to unsubscribe from (optional)");
        unsubscribeCommand.addReturnValue("Array reply: message with unsubscribe status, channel name, and count of remaining subscribed channels");
        unsubscribeCommand.addExample("UNSUBSCRIBE");
        unsubscribeCommand.addExample("UNSUBSCRIBE news");
        
        // PSUBSCRIBE command
        RedisCommand psubscribeCommand = new RedisCommand(
                "psubscribe",
                "PSUBSCRIBE pattern [pattern ...]",
                "Subscribes to channels matching patterns"
        );
        psubscribeCommand.setComplexity(RedisCommandComplexity.O1);
        psubscribeCommand.setSince("2.0.0");
        psubscribeCommand.addParameter("pattern - The first pattern to subscribe to");
        psubscribeCommand.addParameter("pattern ... - Additional patterns to subscribe to");
        psubscribeCommand.addReturnValue("Array reply: message with psubscribe status, pattern, and count of pattern subscriptions");
        psubscribeCommand.addExample("PSUBSCRIBE news.*");
        psubscribeCommand.addExample("PSUBSCRIBE news.* chat.*");
        // PUNSUBSCRIBE command
        RedisCommand punsubscribeCommand = new RedisCommand(
                "punsubscribe",
                "PUNSUBSCRIBE [pattern [pattern ...]]",
                "Unsubscribes from channels matching patterns"
        );
        punsubscribeCommand.setComplexity(RedisCommandComplexity.O1);
        punsubscribeCommand.setSince("2.0.0");
        punsubscribeCommand.addParameter("pattern - The first pattern to unsubscribe from (optional)");
        punsubscribeCommand.addParameter("pattern ... - Additional patterns to unsubscribe from (optional)");
        punsubscribeCommand.addReturnValue("Array reply: message with punsubscribe status, pattern, and count of remaining pattern subscriptions");
        punsubscribeCommand.addExample("PUNSUBSCRIBE");
        punsubscribeCommand.addExample("PUNSUBSCRIBE news.*");
        
        // PUBSUB command
        RedisCommand pubsubCommand = new RedisCommand(
                "pubsub",
                "PUBSUB subcommand [argument [argument ...]]",
                "Introspection command for the Pub/Sub system"
        );
        pubsubCommand.setComplexity(RedisCommandComplexity.O1);
        pubsubCommand.setSince("2.8.0");
        pubsubCommand.addParameter("subcommand - One of: CHANNELS, NUMSUB, NUMPAT");
        pubsubCommand.addParameter("argument - Arguments depending on the subcommand");
        pubsubCommand.addReturnValue("Depends on the subcommand");
        pubsubCommand.addExample("PUBSUB CHANNELS");
        pubsubCommand.addExample("PUBSUB CHANNELS news.*");
        pubsubCommand.addExample("PUBSUB NUMSUB news chat");
        pubsubCommand.addExample("PUBSUB NUMPAT");
        
        // Register commands
        category.addCommand(publishCommand);
        category.addCommand(subscribeCommand);
        category.addCommand(unsubscribeCommand);
        category.addCommand(psubscribeCommand);
        category.addCommand(punsubscribeCommand);
        category.addCommand(pubsubCommand);
        
        // Add to command map
        commandsByName.put(publishCommand.getName(), publishCommand);
        commandsByName.put(subscribeCommand.getName(), subscribeCommand);
        commandsByName.put(unsubscribeCommand.getName(), unsubscribeCommand);
        commandsByName.put(psubscribeCommand.getName(), psubscribeCommand);
        commandsByName.put(punsubscribeCommand.getName(), punsubscribeCommand);
        commandsByName.put(pubsubCommand.getName(), pubsubCommand);
    }
    
    private void addTransactionCommands(RedisCommandCategory category) {
        // MULTI command
        RedisCommand multiCommand = new RedisCommand(
                "multi",
                "MULTI",
                "Marks the start of a transaction block"
        );
        multiCommand.setComplexity(RedisCommandComplexity.O1);
        multiCommand.setSince("1.2.0");
        multiCommand.addReturnValue("Simple string reply: OK");
        multiCommand.addExample("MULTI");
        
        // EXEC command
        RedisCommand execCommand = new RedisCommand(
                "exec",
                "EXEC",
                "Executes all commands issued after MULTI"
        );
        execCommand.setComplexity(RedisCommandComplexity.O1);
        execCommand.setSince("1.2.0");
        execCommand.addReturnValue("Array reply: each element is the result of a command in the transaction, in the same order they were issued");
        execCommand.addReturnValue("Null reply: if the execution was aborted due to a WATCH command");
        execCommand.addExample("EXEC");
        
        // DISCARD command
        RedisCommand discardCommand = new RedisCommand(
                "discard",
                "DISCARD",
                "Discards all commands issued after MULTI"
        );
        discardCommand.setComplexity(RedisCommandComplexity.O1);
        discardCommand.setSince("2.0.0");
        discardCommand.addReturnValue("Simple string reply: OK");
        discardCommand.addExample("DISCARD");
        
        // WATCH command
        RedisCommand watchCommand = new RedisCommand(
                "watch",
                "WATCH key [key ...]",
                "Watches keys for modifications in a transaction"
        );
        watchCommand.setComplexity(RedisCommandComplexity.O1);
        watchCommand.setSince("2.2.0");
        watchCommand.addParameter("key - The first key to watch");
        watchCommand.addParameter("key ... - Additional keys to watch");
        watchCommand.addReturnValue("Simple string reply: OK");
        watchCommand.addExample("WATCH mykey");
        watchCommand.addExample("WATCH key1 key2");
        
        // UNWATCH command
        RedisCommand unwatchCommand = new RedisCommand(
                "unwatch",
                "UNWATCH",
                "Removes all watched keys"
        );
        unwatchCommand.setComplexity(RedisCommandComplexity.O1);
        unwatchCommand.setSince("2.2.0");
        unwatchCommand.addReturnValue("Simple string reply: OK");
        unwatchCommand.addExample("UNWATCH");
        
        // Register commands
        category.addCommand(multiCommand);
        category.addCommand(execCommand);
        category.addCommand(discardCommand);
        category.addCommand(watchCommand);
        category.addCommand(unwatchCommand);
        
        // Add to command map
        commandsByName.put(multiCommand.getName(), multiCommand);
        commandsByName.put(execCommand.getName(), execCommand);
        commandsByName.put(discardCommand.getName(), discardCommand);
        commandsByName.put(watchCommand.getName(), watchCommand);
        commandsByName.put(unwatchCommand.getName(), unwatchCommand);
    }
}
