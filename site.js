function github_api_start(data)
{
	let repourl = data.repository_url
		.replace('{owner}', REPO_OWNER)
		.replace('{repo}', REPO_NAME);
	$INC(repourl + "/releases?callback=display_release");
}

// ================================================================================

$Q1('body').onload = function()
{
	$INCCALL("https://api.github.com/", github_api_start);
	$IncludeHTML();
};

// ================================================================================

function display_release(response)
{
	display_release_data(response.data[0], $Q1('#releases'), $Q1('#release-version'), $Q1('.site-release-links'));
}

function display_release_data(release, release_section_element, release_version_element, release_links_element)
{
	const version = release.name;
	const SORTASSET = function(asset)
	{
		const filename = asset.name;
		if (filename.indexOf('-bash') >= 0)
			return 1;
		else if (filename.indexOf('-cmd') >= 0)
			return 2;
		else
			return 0;
	};

	const GENTITLE = function(filename) 
	{
		if (filename.indexOf('-bash') >= 0)
			return 'Download Bash Version';
		else if (filename.indexOf('-cmd') >= 0)
			return 'Download CMD Version';
		else
			return 'Download';
	};

	release.assets = release.assets.sort((a,b) => {return SORTASSET(a) - SORTASSET(b)});

	$E(release.assets, (asset)=>{
		let linkhtml = [
			GENTITLE(asset.name),
			'<span class="w3-small">'+asset.name+'</span>',
			parseInt(asset.size / 1024) + ' KB',
		].join('<br/>');

		let link = $Element('a', {
			"href": asset.browser_download_url, 
			"class": 'w3-button w3-round-large w3-margin download-link'
		});
		link.innerHTML = linkhtml;

		release_links_element.appendChild($Element('div', {"class":'w3-col l6 w3-center'}, [link]));
	});

	release_version_element.innerHTML = version;
	$ClassRemove(release_section_element, 'site-start-hidden');
}
