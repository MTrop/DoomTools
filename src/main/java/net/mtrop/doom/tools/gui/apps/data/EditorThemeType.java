package net.mtrop.doom.tools.gui.apps.data;

import java.util.Map;

import net.mtrop.doom.tools.struct.util.EnumUtils;

/**
 * Editor Theme types.
 * @author Matthew Tropiano
 */
public enum EditorThemeType
{
	DEFAULT("Default", "org/fife/ui/rsyntaxtextarea/themes/default.xml", false),
	DEFAULT_ALT("Default (Alternate)", "org/fife/ui/rsyntaxtextarea/themes/default-alt.xml", false),
	DARK("Dark", "org/fife/ui/rsyntaxtextarea/themes/dark.xml", true),
	DRUID("Druid", "org/fife/ui/rsyntaxtextarea/themes/druid.xml", true),
	ECLIPSE("Eclipse", "org/fife/ui/rsyntaxtextarea/themes/eclipse.xml", false),
	IDEA("IntelliJ IDEA", "org/fife/ui/rsyntaxtextarea/themes/idea.xml", false),
	MONOKAI("Monokai", "org/fife/ui/rsyntaxtextarea/themes/monokai.xml", true),
	VS("Visual Studio", "org/fife/ui/rsyntaxtextarea/themes/vs.xml", false),
	
	ARC_DARK("Arc Dark", "gui/editor/themes/arc-dark.xml", true),
	ATOM_ONE_DARK("Atom One Dark", "gui/editor/themes/atom-one-dark.xml", true),
	ATOM_ONE_LIGHT("Atom One Light", "gui/editor/themes/atom-one-light.xml", false),
	DRACULA("Dracula", "gui/editor/themes/dracula.xml", true),
	GITHUB("GitHub", "gui/editor/themes/github.xml", false),
	GITHUB_DARK("GitHub Dark", "gui/editor/themes/github-dark.xml", true),
	LIGHT_OWL("Light Owl", "gui/editor/themes/light-owl.xml", false),
	MATERIAL_DARKER("Material Darker", "gui/editor/themes/material-darker.xml", true),
	MATERIAL_DEEP_OCEAN("Material Deep Ocean", "gui/editor/themes/material-deep-ocean.xml", true),
	MATERIAL_LIGHTER("Material Lighter", "gui/editor/themes/material-lighter.xml", false),
	MATERIAL_OCEANIC("Material Oceanic", "gui/editor/themes/material-oceanic.xml", true),
	MATERIAL_PALENIGHT("Material Palenight", "gui/editor/themes/material-palenight.xml", true),
	MOONLIGHT("Moonlight", "gui/editor/themes/moonlight.xml", true),
	NIGHT_OWL("Night Owl", "gui/editor/themes/night-owl.xml", true),
	SOLARIZED_DARK("Solarized Dark", "gui/editor/themes/solarized-dark.xml", true),
	SOLARIZED_LIGHT("Solarized Light", "gui/editor/themes/solarized-light.xml", false),
	ARC_THEME_DARK_ORANGE("Arc Theme Dark - Orange", "gui/editor/themes/arc-theme-dark---orange.xml", true),
	ARC_THEME_DARK("Arc Theme Dark", "gui/editor/themes/arc-theme-dark.xml", true),
	ARC_THEME("Arc Theme", "gui/editor/themes/arc-theme.xml", false),
	ARC_THEME_ORANGE("Arc Theme - Orange", "gui/editor/themes/arc-theme---orange.xml", false),
	CARBON("Carbon", "gui/editor/themes/carbon.xml", true),
	COBALT_2("Cobalt 2", "gui/editor/themes/cobalt-2.xml", true),
	CYAN_LIGHT("Cyan light", "gui/editor/themes/cyan-light.xml", false),
	DARK_FLAT_THEME("Dark Flat Theme", "gui/editor/themes/dark-flat-theme.xml", true),
	DARK_PURPLE("Dark purple", "gui/editor/themes/dark-purple.xml", true),
	GRADIANTO_DARK_FUCHSIA("Gradianto Dark Fuchsia", "gui/editor/themes/gradianto-dark-fuchsia.xml", true),
	GRADIANTO_DEEP_OCEAN("Gradianto Deep Ocean", "gui/editor/themes/gradianto-deep-ocean.xml", true),
	GRADIANTO_MIDNIGHT_BLUE("Gradianto Midnight Blue", "gui/editor/themes/gradianto-midnight-blue.xml", true),
	GRADIANTO_NATURE_GREEN("Gradianto Nature Green", "gui/editor/themes/gradianto-nature-green.xml", true),
	GRAY("Gray", "gui/editor/themes/gray.xml", false),
	GRUVBOX_DARK_HARD("Gruvbox Dark Hard", "gui/editor/themes/gruvbox-dark-hard.xml", true),
	HIBERBEE_DARK("Hiberbee Dark", "gui/editor/themes/hiberbee-dark.xml", true),
	HIGH_CONTRAST("High Contrast", "gui/editor/themes/high-contrast.xml", true),
	LIGHT_FLAT("Light Flat", "gui/editor/themes/light-flat.xml", false),
	NOTREALLYMDTHEME("NotReallyMDTheme", "gui/editor/themes/notreallymdtheme.xml", true),
	MONOCAI("Monocai", "gui/editor/themes/monocai.xml", true),
	MONOKAI_PRO("Monokai Pro", "gui/editor/themes/monokai-pro.xml", true),
	NORD("Nord", "gui/editor/themes/nord.xml", true),
	ONE_DARK("One Dark", "gui/editor/themes/one-dark.xml", true),
	SPACEGRAY("Spacegray", "gui/editor/themes/spacegray.xml", true),
	VUESION_THEME("vuesion-theme", "gui/editor/themes/vuesion-theme.xml", true),
	XCODE_DARK("Xcode-Dark", "gui/editor/themes/xcode-dark.xml", true),

	;
	
	private final String friendlyName;
	private final String resourceName;
	private final boolean dark;
	
	public static final Map<String, EditorThemeType> THEME_MAP = EnumUtils.createCaseInsensitiveNameMap(EditorThemeType.class);
	
	private EditorThemeType(String friendlyName, String resourceName, boolean dark)
	{
		this.friendlyName = friendlyName;
		this.resourceName = resourceName;
		this.dark = dark;
	}
	
	public String getResourceName()
	{
		return resourceName;
	}
	
	public String getFriendlyName() 
	{
		return (dark ? "(Dark)" : "(Light)") + " " + friendlyName;
	}
	
	public boolean isDark() 
	{
		return dark;
	}
}

