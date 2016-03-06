use warnings;

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
$OUTPUT_FILE = "output.data";
$LOGIN = "110971610";
$PASSWORD = "50c20b94";

##################
# main program #
##################

my $cookie = login();
system "curl -s --header \"" . $cookie . "\" -o \"" . $DATA_FILE . "\" " . $DATA_URL;

my $routes = processJSON(time);
print_routes($routes, $OUTPUT_FILE);


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
####	- parses the JSON in $DATA_FILE
####	- extracts only neccesary data and put them in route-entries
####	- returns a hashmap with all route-entries as hashmap:
####		* keys in hashmap are numeric route_id's
####		* keys in entries are provider_name, provider_id, timestamp,
####			route_id, route_name, normal_time, real_time, length,
####			start_lat, start_lon, end_lat and end_lon
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
		my @wrapper_locations = @{$routes{$key}{"geometries"}};
		my @locations = @{$wrapper_locations[0]};
		my %start_loc = %{$locations[0]};
		my %end_loc = %{$locations[-1]};
		my $start_lat = $start_loc{"lat"};
		my $start_lon = $start_loc{"lng"};
		my $end_lat = $end_loc{"lat"};
		my $end_lon = $end_loc{"lng"};

		# store all found data in entry-object
		my %entry = (
			provider_name => $PROVIDER_NAME,
			provider_id => $PROVIDER_ID,
			timestamp => $timestamp,
			route_id => $route_id,
			route_name => $key,
			normal_time => $routes{$key}{'normal_time'},
			real_time => $routes{$key}{'real_time'},
			length => $routes{$key}{'length'},
			start_lat=> $start_lat,
			start_lon=> $start_lon,
			end_lat=> $end_lat,
			end_lon=> $end_lon,
			);
		$entries{$route_id}=\%entry;
	}


	return \%entries;
}


###	print_routes :: void
####	- prints all route entries contained in the first param 
####		to standard output or a file if a file name is provided
sub print_routes {
	my $routes = $_[0]; # get hasmap with all route-entries	
	my $filename = $_[1]; # get filename if a file is provided
	my $file;

	if(defined($filename)){ # a filename is provided, open the file for writing (overwrites content in file provided
		open( FILEHANDLE , '>'.$filename );
		$file = *FILEHANDLE;
	} else { # no filename provided, print to the console
		$file = *STDOUT;
	}

	for $route (keys %{$routes}){ # for each route-entry
		%entry = %{$routes->{$route}};
		print $file "ENTRY\n";
		
		for $key (sort keys %entry){ # for each object in route-entry
			print $file "$key: $entry{$key}\n";
		}
		
		print $file "END\n\n";
	}
	
	close($file) if defined($filename);
}
