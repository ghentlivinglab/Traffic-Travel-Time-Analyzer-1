# Dit scriptje pullt van de server en zal de scss automatisch compileren naar css
print "Git pull...\n";
print `cd ../ && git pull`;
if ("$?" != 0){
	die (" Mislukt. \n");
}else{
	print " Ok.\n\n";
}


print "SCSS compileren...\n";
print `sass ../VerkeerREST/web/sass/style.scss ../VerkeerREST/web/css/style.css --scss --sourcemap=none --style=compressed`;
if ("$?" != 0){
	die (" Mislukt. \n Zie SASS documentatie op sass-lang.com\n");
}else{
	print " Ok.\n";
}