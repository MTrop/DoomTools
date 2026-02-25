package net.mtrop.doom.tools.struct;

import java.io.IOException;

public final class PreprocessorLexerTest 
{
	public static void main(String[] args) throws IOException
	{
		PreprocessorLexer plexer = new PreprocessorLexer(new PreprocessorLexer.Kernel() {{
			setDecimalSeparator('.');
			addStringDelimiter('"', '"');
			setEmitNewlines(true);
		}}, "Test", (new StringBuilder())
				.append("#define butt 123").append('\n')
				.append("#define linetest 123\\").append('\n')
				.append("butt\\").append('\n')
				.append("123").append('\n')
				.append("#define fart butt").append('\n')
				.append("butt").append('\n')
				.append("buttbutt").append('\n')
				.append("hello linetest hello").append('\n')
				.append("fart").append('\n')
				.append("#define start \"this is").append('\n')
				.append("#define end string\"").append('\n')
				.append("\"this is a string\"").append('\n')
				.append("\"start a end\"").append('\n')
				.append("start a end").append('\n')
			.toString()
		);
		
		PreprocessorLexer.Token token;
		while ((token = plexer.nextToken()) != null)
			System.out.println(token);
	}
}
