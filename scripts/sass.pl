print "Dit programma compileert onze scss bestanden naar css.\n";
system("sass ../VerkeerREST/web/sass/style.scss ../VerkeerREST/web/css/style.css --scss --sourcemap=none --style=compressed");
if ($? == -1) {
    print "Compileren mislukt. Zorg ervoor dat SASS is geïnstalleerd.\n - $!\n";
}else{
    print "Klaar.\n";
}
