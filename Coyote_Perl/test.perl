#use warnings;

use JSON qw( decode_json );
# to use this library, make sure it is installed in your Perl-system
# if "perl -MJSON -e 1" gives no errors the library is already installed
# else use "sudo perl -MCPAN -e 'install JSON'" to install library

####################
# global variables #
####################

$PROVIDER_ID = "";
$PROVIDER_NAME = "Coyote";
$MAIN_URL = "https://maps.coyotesystems.com/traffic/index.php";
$DATA_URL = "https://maps.coyotesystems.com/traffic/ajax/get_perturbation_list.ajax.php";
$HEADER_FILE = "header.txt";
$DATA_FILE = "data.json";
$LOGIN = "110971610";
$PASSWORD = "50c20b94";

##################
# main program #
##################

my $cookie = login();
system "curl -s --header \"" . $cookie . "\" -o \"" . $DATA_FILE . "\" " . $DATA_URL;
my $routes = processJSON(time);
for $route (keys %{$routes}){
	print "$routes->{$route}{route_name}\n";
	%entry = %{$routes->{$route}};
	for $key (keys %entry){
		print "-- $key => $entry{$key}\n";
	}
}


###############
# subroutines #
###############

### login() :: String cookie
####	- logs in into coyote server
####	- stores all headers in $DATA_FILE
#### 	- returns the http-header with the session-cookie
sub login {	
	my $cookie;

	# reads headers to $HEADER_FILE and ignores body
	system "curl -s -d \"login=" . $LOGIN . "&password=" . $PASSWORD . "\" -D " . $HEADER_FILE . " -o /dev/null " . $MAIN_URL;
	
	# get all headers in memory
	open(LOGINHEADER,"header.txt"); # open file
	my @lines = <LOGINHEADER>; # get all headers and store in array
	close(LOGINHEADER); # close file


	#find the session and store in $cookie
	foreach my $line (@lines){
		if($line=~ m{^Set-Cookie: (.*); path.*$}){
			$cookie= $1;
		}
	}

	#return the session
	return "Cookie: " . $cookie;
}

###	processJSON() :: HashMap entries
####	- 
####	- 
####	- returns an hashmap with all route-entries as hashmap
sub processJSON {
	#gets the moment that the data was gathered
	my $timestamp = $_[0];

	#get the json in memory
	open(JSONDATA,$DATA_FILE);
	local $/=undef;
	my $json = <JSONDATA>;
	close(JSONDATA);

	# decode json-string to native Perl-structure
	my %data = %{ decode_json( $json) };
	%routes = %{$data{"Gand"}};

	my %entries;
	my $test_id=0;
	# process each route
	foreach my $key (keys %routes){
		my $route_id=$test_id;
		$test_id++;
		my $startlat;
		my $startlon;
		my $endlat;
		my $endlon;

		my %entry = (
			provider_name => $PROVIDER_NAME,
			provider_id => $PROVIDER_ID,
			timestamp => $timestamp,
			route_id => $route_id,
			route_name => $key,
			normal_time => $routes{$key}{'normal_time'},
			real_time => $routes{$key}{'real_time'},
			length => $routes{$key}{'length'},
			startlat=> $startlat,
			startlon=> $startlon,
			endlat=> $endlat,
			endlon=> $endlon,
			);
		$entries{$route_id}=\%entry;
	}


	return \%entries;
}
