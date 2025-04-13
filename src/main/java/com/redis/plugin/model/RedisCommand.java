package com.redis.plugin.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a Redis command with its documentation
 */
public class RedisCommand {
    private final String name;
    private final String syntax;
    private final String description;
    private final List<String> examples;
    private final List<String> parameters;
    private final List<String> returnValues;
    private RedisCommandComplexity complexity;
    private String since;

    /**
     * Create a new Redis command
     * 
     * @param name the command name (e.g., "GET")
     * @param syntax the command syntax (e.g., "GET key")
     * @param description the command description
     */
    public RedisCommand(String name, String syntax, String description) {
        this.name = name;
        this.syntax = syntax;
        this.description = description;
        this.examples = new ArrayList<>();
        this.parameters = new ArrayList<>();
        this.returnValues = new ArrayList<>();
    }

    /**
     * Get the command name
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the command syntax
     * 
     * @return the syntax
     */
    public String getSyntax() {
        return syntax;
    }

    /**
     * Get the command description
     * 
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get the command examples
     * 
     * @return an unmodifiable list of examples
     */
    public List<String> getExamples() {
        return Collections.unmodifiableList(examples);
    }

    /**
     * Add an example of command usage
     * 
     * @param example the example to add
     */
    public void addExample(String example) {
        this.examples.add(example);
    }

    /**
     * Get the command parameters
     * 
     * @return an unmodifiable list of parameters
     */
    public List<String> getParameters() {
        return Collections.unmodifiableList(parameters);
    }

    /**
     * Add a parameter description
     * 
     * @param parameter the parameter description
     */
    public void addParameter(String parameter) {
        this.parameters.add(parameter);
    }

    /**
     * Get the command return values
     * 
     * @return an unmodifiable list of return values
     */
    public List<String> getReturnValues() {
        return Collections.unmodifiableList(returnValues);
    }

    /**
     * Add a return value description
     * 
     * @param returnValue the return value description
     */
    public void addReturnValue(String returnValue) {
        this.returnValues.add(returnValue);
    }

    /**
     * Get the command time complexity
     * 
     * @return the complexity
     */
    public RedisCommandComplexity getComplexity() {
        return complexity;
    }

    /**
     * Set the command time complexity
     * 
     * @param complexity the complexity to set
     */
    public void setComplexity(RedisCommandComplexity complexity) {
        this.complexity = complexity;
    }

    /**
     * Get the Redis version when this command was introduced
     * 
     * @return the version string
     */
    public String getSince() {
        return since;
    }

    /**
     * Set the Redis version when this command was introduced
     * 
     * @param since the version string
     */
    public void setSince(String since) {
        this.since = since;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RedisCommand command = (RedisCommand) o;
        return Objects.equals(name, command.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
