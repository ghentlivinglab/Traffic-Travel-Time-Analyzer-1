#logs in into coyote server and returns the Cookie
sub login{
	system "curl -s -d \"login=110971610&password=50c20b94\" -D header.txt -o /dev/null https://maps.coyotesystems.com/traffic/index.php"; # reads headers to header.txt and ignores body
	open(LOGINHEADER,"header.txt"); # open file met headers
	my @lines = <LOGINHEADER>; # get all headers
	my $cookie;
	#find the session and store in $cookie
	foreach $line (@lines){
		if($line=~ m{^Set-Cookie: (.*); path.*$}){
			$cookie= $1;
		}
	}
	return "Cookie: ". $cookie;
}

my $cookie = login();
system "curl -s --header \"".$cookie."\" -o data.json https://maps.coyotesystems.com/traffic/ajax/get_perturbation_list.ajax.php";
