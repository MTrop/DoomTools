package net.mtrop.doom.tools.doommake;

import java.util.function.Function;

/**
 * A single project token replacer entry for filling tokens after project creation.
 * This contains the prompting message as well for generating the replace content.
 * @author Matthew Tropiano
 */
public class ProjectTokenReplacer 
{
	/** Default sanitizer. */
	public static final Function<String, String> DEFAULT_SANITIZER = (s) -> s;	
	/** Default, "accept all" validator. */
	public static final Function<String, String> DEFAULT_VALIDATOR = (s) -> null;	
	
	/** Token name. */
	private String token;
	/** Token prompt. */
	private String prompt;
	/** Default if blank. */
	private String defaultValue;
	/** Input sanitation function. */
	private Function<String, String> sanitizer;
	/** Input validation function. */
	private Function<String, String> validator;
	
	private ProjectTokenReplacer(String token, String prompt, String defaultValue, Function<String, String> sanitation, Function<String, String> validator) 
	{
		this.token = token;
		this.prompt = prompt;
		this.sanitizer = sanitation;
		this.validator = validator;
		this.defaultValue = defaultValue;
	}
	
	/**
	 * Creates a token replacer prompt.
	 * @param token the token name to replace in each file.
	 * @param prompt the prompt for getting the value.
	 * @return the new replacer.
	 */
	public static ProjectTokenReplacer create(String token, String prompt)
	{
		return new ProjectTokenReplacer(token, prompt, "", DEFAULT_SANITIZER, DEFAULT_VALIDATOR);
	}
	
	/**
	 * Creates a token replacer prompt.
	 * @param token the token name to replace in each file.
	 * @param prompt the prompt for getting the value.
	 * @param sanitizer the input sanitizer.
	 * @return the new replacer.
	 */
	public static ProjectTokenReplacer create(String token, String prompt, Function<String, String> sanitizer)
	{
		return new ProjectTokenReplacer(token, prompt, "", sanitizer, DEFAULT_VALIDATOR);
	}
	
	/**
	 * Creates a token replacer prompt.
	 * @param token the token name to replace in each file.
	 * @param prompt the prompt for getting the value.
	 * @param defaultValue the value to use if the field is blank.
	 * @return the new replacer.
	 */
	public static ProjectTokenReplacer create(String token, String prompt, String defaultValue)
	{
		return new ProjectTokenReplacer(token, prompt, defaultValue, DEFAULT_SANITIZER, DEFAULT_VALIDATOR);
	}
	
	/**
	 * Creates a token replacer prompt.
	 * @param token the token name to replace in each file.
	 * @param prompt the prompt for getting the value.
	 * @param defaultValue the value to use if the field is blank.
	 * @param validator the validator to use (if it returns null, valid input. If not, a message is returned).
	 * @return the new replacer.
	 */
	public static ProjectTokenReplacer create(String token, String prompt, String defaultValue, Function<String, String> validator)
	{
		return new ProjectTokenReplacer(token, prompt, defaultValue, DEFAULT_SANITIZER, validator);
	}
		
	/**
	 * Creates a token replacer prompt.
	 * @param token the token name to replace in each file.
	 * @param prompt the prompt for getting the value.
	 * @param defaultValue the value to use if the field is blank.
	 * @param sanitizer the input sanitizer.
	 * @param validator the validator to use (if it returns null, valid input. If not, a message is returned).
	 * @return the new replacer.
	 */
	public static ProjectTokenReplacer create(String token, String prompt, String defaultValue, Function<String, String> sanitizer, Function<String, String> validator)
	{
		return new ProjectTokenReplacer(token, prompt, defaultValue, sanitizer, validator);
	}

	public String getToken() 
	{
		return token;
	}

	public String getPrompt() 
	{
		return prompt;
	}

	public String getDefaultValue() 
	{
		return defaultValue;
	}

	public Function<String, String> getSanitizer() 
	{
		return sanitizer;
	}
	
	public Function<String, String> getValidator() 
	{
		return validator;
	}
	
	public boolean equals(ProjectTokenReplacer obj) 
	{
		return token.equals(obj.defaultValue);
	}
	
	@Override
	public boolean equals(Object obj) 
	{
		if (obj instanceof ProjectTokenReplacer)
			return equals((ProjectTokenReplacer)obj);
		return super.equals(obj);
	}
	
}
