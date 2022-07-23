const BodyElement = $DJ.tag('body')[0];

var $INC = $INC || function(srcurl)
{
	fetch(srcurl)
	    .then(resp => resp.text())
	    .then(content => {
	    	BodyElement.appendChild($DJ.e('script', {
			    "type": 'text/javascript'
		    }, [$DJ.t(content)]));
		});
};

var $INCCALL = $INCCALL || function(srcurl, callback)
{
	fetch(srcurl)
	    .then(resp => resp.json())
	    .then(json => {
	    	callback(json);
		});
};

// W3Schools Include Hook, 
// ...but tweaked a little 
// https://www.w3schools.com/howto/howto_html_include.asp
function $IncludeHTML() 
{
	let z, i, elmnt, file, xhttp;
	// Loop through a collection of all HTML DIVs:
	z = document.getElementsByTagName("div");
	for (i = 0; i < z.length; i++) 
	{
		elmnt = z[i];
		// search for elements with a certain atrribute:
		file = elmnt.getAttribute("include-html");
		if (file)
		{
			// Make an HTTP request using the attribute value as the file name:
			xhttp = new XMLHttpRequest();
			xhttp.onreadystatechange = function()
			{
				if (this.readyState == 4)
				{
					if (this.status == 200)
						elmnt.innerHTML = this.responseText;
					if (this.status == 404)
						elmnt.innerHTML = "Page not found.";
					// Remove the attribute, and call this function once more:
					elmnt.removeAttribute("include-html");
					$IncludeHTML();
				}
			}
			xhttp.open("GET", file, true);
			xhttp.send();
			return;
		}
	}
}

function github_api_start(data)
{
	let repourl = data.repository_url
		.replace('{owner}', REPO_OWNER)
		.replace('{repo}', REPO_NAME);
	$INC(repourl + "/releases?callback=display_release");
}

// ================================================================================

BodyElement.onload = function()
{
	$INCCALL("https://api.github.com/", github_api_start);
	$IncludeHTML();
};

// ================================================================================

function display_release(response)
{
	display_release_data(response.data[0], $DJ.id('releases'), $DJ.id('release-version'), $DJ('.site-release-links'));
}

function display_release_data(release, release_section_element, release_version_element, release_links_element)
{
	const version = release.name;
	const SORTASSET = function(asset)
	{
		const filename = asset.name;
		if (filename.indexOf('-jar') >= 0)
			return 5;
		else if (filename.indexOf('-bash') >= 0)
			return 4;
		else if (filename.indexOf('-cmd') >= 0)
			return 3;
		else if (filename.indexOf('-setup-jre') >= 0)
			return 1;
		else if (filename.indexOf('-setup') >= 0)
			return 2;
		else
			return 0;
	};

	const GENTITLE = function(filename) 
	{
		if (filename.indexOf('-jar') >= 0)
			return 'Standalone JAR';
		else if (filename.indexOf('-bash') >= 0)
			return 'Bash (macOS/Linux/Cygwin) Version';
		else if (filename.indexOf('-cmd') >= 0)
			return 'CMD (Windows) Version';
		else if (filename.indexOf('-setup-jre') >= 0)
			return 'Windows Installer (x64)';
		else if (filename.indexOf('-setup') >= 0)
			return 'Windows Installer (no JRE)';
		else
			return 'Download';
	};

	release.assets = release.assets.sort((a,b) => {return SORTASSET(a) - SORTASSET(b)});

	$DJ.each(release.assets, (asset)=>{
		let linkhtml = [
			GENTITLE(asset.name),
			'<span class="w3-small">'+asset.name+'</span>',
			parseInt(asset.size / 1024) + ' KB',
		].join('<br/>');

		let link = $DJ.e('a', {
			"href": asset.browser_download_url, 
			"class": asset.name.indexOf('-setup-jre') >= 0 
				? 'w3-button w3-xlarge w3-round-large w3-margin download-link'
				: 'w3-button w3-round-large w3-margin download-link'
		});
		link.innerHTML = linkhtml;

		if (asset.name.indexOf('-setup-jre') >= 0)
			release_links_element.append($DJ.e('div', {"class":'w3-col l12 w3-center'}, [link]));
		else
			release_links_element.append($DJ.e('div', {"class":'w3-col l6 w3-center'}, [link]));
	});

	release_version_element[0].innerHTML = version;
	release_section_element
		.classRemove('site-start-hidden')
		.classAdd('w3-animate-opacity');
	$DJ.id('releases-loading').classAdd('w3-hide');
}
