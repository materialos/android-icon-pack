# XML Tags
$xml = '<?xml version="1.0" encoding="utf-8"?>'
$resourcesS = '<resources>'
$resourcesE = '</resources>'
$version = '<version>1</version>'
$drawableItemS = '    <item drawable="'
$drawableItemE = '" />'
$iconPackLatestS = '     <string-array name="latest">'
$iconPackAllS = '    <string-array name="icon_pack_names" translatable="false">'
$iconPackItemS = '        <item>'
$iconPackItemE = '</item>'
$stringArrayE = '</string-array>'
$appFilterItemS = '<item
        component="ComponentInfo{'
$appFilterItemI = '}"
        drawable="'
$appFilterItemE = '" />'
$appMapS = '<appmap>'
$appMapE = '</appmap>'
$appMapItemS = '<item
        name="'
$appMapItemI = '"
        class="'
$appMapItemE = '" />'
$themeResourcesS = '<Theme version="1">
    <Label value="AppName" />
    <Wallpaper image="wallpaper" />
    <LockScreenWallpaper image="wallpaper" />
    <ThemePreview image="preview1" />
    <ThemePreviewWork image="preview1" />
    <ThemePreviewMenu image="preview1" />
    <DockMenuAppIcon selector="drawer_icon" />'
$themeResourcesE = '</Theme>'
$themeResourcesItemS = '    <AppIcon
        name="'
$themeResourcesItemI = '"
        image="'
$themeResourcesItemE = '" />'

# Directories
$project = '../Project/MaterialOS'
$res = '/app/src/main/res/drawable-nodpi'
$iconPack = 'values/strings_icon_pack.xml'
$drawable = 'xml/drawable.xml'
$appFilter = 'xml/appfilter.xml'
$appMap = 'xml/appmap.xml'
$themeResources = 'xml/theme_resources.xml'

# Arrays
$ingoredFiles = ["iconback", "iconmask", "iconupon"]
$ingoredPrefixes = ["ic_"]
$wallpaperPrefixes = ["wallpaper"]
$calendarPrefixes = []
$drawableExtensions = [".jpg", ".png"]
$modifiers = ["alt"]
$designers = ["Alex Mueller", "Anas Khan", "Brian Medina", "Christopher Bravata", "Corbin Crutchley", "Createme", "Daniel Ciao", "Daniel Hickman", "Eduardo Pratti", "Gabriel Zegarra", "Greg Ives", "Greg Ives & Daniel Hickman", "Jahir Fiquitiva & Corbin Crutchley", "Jireh Mark Morilla", "Labib Muhammad", "Micheal Cook", "Niko Pennanen", "Oscar E", "Patryk Goworowski", "Sky Konig", "Vukasin Andelkovic", "Wayne Kosimoto", "Wayne Kosimoto & Corbin Crutchley", "Zachary Pierson"]


def getDrawables
	$drawables = []
	var = Dir[$project + $res + "/*"].each {|s| $drawables << s.gsub($project + $res + "/", "").gsub(/#{$drawableExtensions.join("|")}/i, "")}
end
def getOldXML
	
end
getDrawables