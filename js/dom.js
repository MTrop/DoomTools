
var $E = $E || function(list, func)
{
	for (let x in list) if (list.hasOwnProperty(x))
		func(list[x], x, list.length);
};

var $Q = $Q || function(x)
{
	return document.querySelectorAll(x);
};

var $Q1 = $Q1 || function(x)
{
	return document.querySelector(x);
};

var $QE = $QE || function(selector, func)
{
	$E($Q(selector), func);
};

var $INC = $INC || function(srcurl)
{
	fetch(srcurl)
	    .then(resp => resp.text())
	    .then(content => {
	    	$Q1('body').appendChild($Element('script', {
			    "type": 'text/javascript'
		    }, [$Text(content)]));
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

var $ClassAdd = $ClassAdd || function(elem, name)
{
	let classes = elem.className.trim().length > 0 ? elem.className.split(/\s+/) : [];
	if (classes.indexOf(name) < 0)
	{
		classes.push(name);
		elem.className = classes.join(" ");
	}
}

var $ClassRemove = $ClassRemove || function(elem, name)
{
	let classes = elem.className.trim().length > 0 ? elem.className.split(/\s+/) : [];
	let index = -1;
	if ((index = classes.indexOf(name)) >= 0)
	{
		classes.splice(index, 1);
		elem.className = classes.join(" ");
	}
}

var $ClassHas = $ClassHas || function(elem, name)
{
	let classes = elem.className.trim().length > 0 ? elem.className.split(/\s+/) : [];
	return classes.indexOf(name) >= 0;
}

var $ClassToggle = $ClassToggle || function(elem, name)
{
	if ($ClassHas(elem, name))
		$ClassRemove(elem, name);
	else
		$ClassAdd(elem, name);
}

// text: text in node
var $Text = $Text || function(text)
{
	return document.createTextNode(text);
};

// name: tagname
// attribs: object {attrname: 'value'}
// children: array of elements/nodes to append in order
var $Element = $Element || function(name, attribs, children)
{
	let out = document.createElement(name);
	if (attribs) for (let a in attribs) if (attribs.hasOwnProperty(a))
	{
		let attrObj = document.createAttribute(a);
		attrObj.value = attribs[a];
		out.setAttributeNode(attrObj);
	}
	if (children) for (let i = 0; i < children.length; i++)
		out.appendChild(children[i]);
	
	return out;
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
