<?php
include "b.php";
ini_set('max_execution_time', 60000); 

function findGames($date)
{

    $dom = new DOMDocument('1.0');
	$url = "http://www.nba.com/gameline/".$date."/";
	@$dom->loadHTMLFile($url);

    $anchors = $dom->getElementsByTagName('a');
	$i = 0;
    $oldPath = "";
	$games = array();
    foreach ($anchors as $element) 
    {

        $href = $element->getAttribute('href');
        if (0 !== strpos($href, 'http')) {
            $path = '/' . ltrim($href, '/');
			//echo "path is:" . $path;
	
		}//if
		 if(strpos($path, $date) && $oldPath != $path)
		{
				
			$games[$i] = "http://nba.com". $path;
			echo $games[$i] , "<br>";
			///
			$away =  substr($path, 16, 3);
			$home = substr($path, 19, 3);
			///
			crawl_page($games[$i] , $date, $away, $home);
			$oldPath = $path;

			$i +=1;	
		}
    }//foreach
			
}
function crawl_page($url, $date, $away, $home)
{
	$servername = "localhost";
	$username = "root";
	$password = "";
	$dbname = "2014-15";
	$team1 ="";
	$team2 = "";
	$matchup = -1;
	$game =-1;
	$playerID = -1;

	if($home == "WAS")
	{
		echo "detected";
		$home = "WSH";
	}
	if($away == "WAS")
	{
		echo "detected";
		$away = "WSH";
	}


	if(strcmp($away, $home) < 0 )
	{
		$team1 = $away;
		$team2 = $home;
	}
	else
	{
		$team1 = $home;
		$team2 = $away;
	}
	echo "team2 is:" . $team2. "---";
	// Create connection
	$conn = new mysqli($servername, $username, $password, $dbname);
	// Check connection
	if ($conn->connect_errno) {
		die("Connection failed: " . $conn->connect_error);
	} 
	echo $home." vs". $away;
	$sql = "Select matchupID FROM matchup where team1='".$team1."'and team2='".$team2."'";  
		
	$retval = $conn->query($sql);
	//echo $retval;
	while($row = $retval->fetch_assoc())
	{
		echo "matchup ID :".$row["matchupID"]."  <br> ";
		$matchup = $row["matchupID"];
	} 
	////////////////////////////////////////////////

	$sql ="INSERT IGNORE INTO game (matchupID, date, location)
		VALUES ('".$matchup. "', '". $date. "','" . $home."')";
	if ($conn->query($sql) === TRUE) {
		echo "New game created successfully";
	} else {
		echo "Error: " . $sql . "<br>" . $conn->error;
	}///////////////////
	$sql = "Select gameID FROM game where matchupID='".$matchup."'and date='".$date."'";  
		
	$retval = $conn->query($sql);
	//echo $retval;
	while($row = $retval->fetch_assoc())
	{
		echo "gameID :".$row["gameID"]."  <br> ";
		$game = $row["gameID"];
	} 
	
	////////////
	
    $dom = new DOMDocument('1.0');
	@$dom->loadHTMLFile($url);

    $anchors = getElementsByClass($dom,'tr','odd');
	$i = 0;
	$seprator = $dom->getElementsByTagName('thead');
	echo "Away: <br>"; 
    foreach ($anchors as $element) 
    {
		if($element->getLineNo() < $seprator[1]->getLineNo())
		{
			//$playerName = rtrim( str_replace("_", " ", rtrim( substr( $element->getElementsByTagName('a')[0]->getAttribute('href'), 12), 
				//substr( substr( $element->getElementsByTagName('a')[0]->getAttribute('href'), 12) , -10))), "/");
				 
			if($element->getElementsByTagName('a')[0] == null)
				continue;
			echo "node is:",str_replace("_"," ",substr( substr( $element->getElementsByTagName('a')[0]->getAttribute('href'), 12) ,0 , -11));
			$playerName = str_replace("_"," ",substr( substr( $element->getElementsByTagName('a')[0]->getAttribute('href'), 12) ,0 , -11));
			$words = explode( ' ', $playerName);
			array_shift( $words);
			$lastName = implode( ' ', $words);
			$firstName = explode(" ", $playerName);
			
			//echo "represent is:" , $variable;
			
			echo "@",$element->nodeValue,"<br>";
			$token = strtok($element->nodeValue, " ");
			////////////////////
			$sql = "INSERT IGNORE INTO player (teamID, firstName, lastName) VALUES ('" .$away. "','". $firstName[0]. "','" . $lastName."')";
			if ($conn->query($sql) === TRUE) {
				echo "New record created successfully";
			}else
				echo "Error: " . $sql . "<br>" . $conn->error;
			
				$sql = "Select playerID FROM player where firstName='".$firstName[0]."'and lastName='".$lastName."'";  

			$retval = $conn->query($sql);
			//echo $retval;
			while($row = $retval->fetch_assoc())
			{
				echo "playerID :".$row["playerID"]."  <br> ";
				$playerID = $row["playerID"];
			} 	
			$sql = "INSERT IGNORE INTO playerstats (playerID, gameID) VALUES ('" .$playerID. "','". $game."')";
			if ($conn->query($sql) === TRUE) {
				echo "New record created successfully";
			}else
				echo "Error: " . $sql . "<br>" . $conn->error;			
			///////////////
			$element->nodeValue = substr( $element->nodeValue , strpos($element->nodeValue, ":")-2);
			if($element->nodeValue == "" || $element->nodeValue == FALSE )
			{
				//echo "here";
				continue;
			}
			echo "here";
			$token = strtok($element->nodeValue, " ");
			if(strpos( $token,"00:00") !== false)
				continue;
			$counter = 0;
			$DNP = 0;
			while ($token !== false)
			{	
				
				switch($counter)
				{
					case 0:
						$sql = "UPDATE playerstats SET min ='".$token."'where playerID='". $playerID."'AND gameID='".$game."'";
						if ($conn->query($sql) === TRUE) {
							echo "updated succesfully here";
						}else
							echo "Error: " . $sql . "<br>" . $conn->error;
						break;
				
					case 1:
						$fg = explode('-', $token);
						echo "fgATTEMP is: ". $fg[1];
						echo "fgMADE is: ". $fg[0];
						$sql = "UPDATE playerstats SET fgm ='".$fg[0]."', fga ='".$fg[1]."'where playerID='". $playerID."'AND gameID='".$game."'";
						if ($conn->query($sql) === TRUE) {
							echo "New record created successfully";
						}else
							echo "Error: " . $sql . "<br>" . $conn->error;
						break;
					case 2:
						$fg = explode('-', $token);
						$sql = "UPDATE playerstats SET 3pm ='".$fg[0]."', 3pa ='".$fg[1]."'where playerID='". $playerID."'AND gameID='".$game."'";
						if ($conn->query($sql) === TRUE) {
							echo "New record created successfully";
						}else
							echo "Error: " . $sql . "<br>" . $conn->error;
						break;		
					case 3:
						$fg = explode('-', $token);
						$sql = "UPDATE playerstats SET ftm ='".$fg[0]."', fta ='".$fg[1]."'where playerID='". $playerID."'AND gameID='".$game."'";
						if ($conn->query($sql) === TRUE) {
							echo "New record created successfully";
						}else
							echo "Error: " . $sql . "<br>" . $conn->error;
						break;					
					case 4:
						$sql = "UPDATE playerstats SET pm ='".$token."'where playerID='". $playerID."'AND gameID='".$game."'";
						if ($conn->query($sql) === TRUE) {
							echo "New record created successfully";
						}else
							echo "Error: " . $sql . "<br>" . $conn->error;
						break;			
					case 5:
						$sql = "UPDATE playerstats SET ORB ='".$token."'where playerID='". $playerID."'AND gameID='".$game."'";
						if ($conn->query($sql) === TRUE) {
							echo "New record created successfully";
						}else
							echo "Error: " . $sql . "<br>" . $conn->error;
						break;	
					case 6:
						$sql = "UPDATE playerstats SET DRB ='".$token."'where playerID='". $playerID."'AND gameID='".$game."'";
						if ($conn->query($sql) === TRUE) {
							echo "New record created successfully";
						}else
							echo "Error: " . $sql . "<br>" . $conn->error;
						break;	
					case 7:
						$sql = "UPDATE playerstats SET reb ='".$token."'where playerID='". $playerID."'AND gameID='".$game."'";
						if ($conn->query($sql) === TRUE) {
							echo "New record created successfully";
						}else
							echo "Error: " . $sql . "<br>" . $conn->error;
						break;	
					case 8:
						$sql = "UPDATE playerstats SET ast ='".$token."'where playerID='". $playerID."'AND gameID='".$game."'";
						if ($conn->query($sql) === TRUE) {
							echo "New record created successfully";
						}else
							echo "Error: " . $sql . "<br>" . $conn->error;
						break;			
					case 9:
						$sql = "UPDATE playerstats SET pf ='".$token."'where playerID='". $playerID."'AND gameID='".$game."'";
						if ($conn->query($sql) === TRUE) {
							echo "New record created successfully";
						}else
							echo "Error: " . $sql . "<br>" . $conn->error;
						break;	
					case 10:
						$sql = "UPDATE playerstats SET stl ='".$token."'where playerID='". $playerID."'AND gameID='".$game."'";
						if ($conn->query($sql) === TRUE) {
							echo "New record created successfully";
						}else
							echo "Error: " . $sql . "<br>" . $conn->error;
						break;	
					case 11:
						$sql = "UPDATE playerstats SET tov ='".$token."'where playerID='". $playerID."'AND gameID='".$game."'";
						if ($conn->query($sql) === TRUE) {
							echo "New record created successfully";
						}else
							echo "Error: " . $sql . "<br>" . $conn->error;
						break;	
					case 12:
						$sql = "UPDATE playerstats SET bs ='".$token."'where playerID='". $playerID."'AND gameID='".$game."'";
						if ($conn->query($sql) === TRUE) {
							echo "New record created successfully";
						}else
							echo "Error: " . $sql . "<br>" . $conn->error;
						break;			
					case 13:
						break;			
					case 14:
						$sql = "UPDATE playerstats SET pts ='".$token."'where playerID='". $playerID."'AND gameID='".$game."'";
						if ($conn->query($sql) === TRUE) {
							echo "New record created successfully";
						}else
							echo "Error: " . $sql . "<br>" . $conn->error;
						break;			
					}
				echo $counter , "$token";
				$token = strtok(" ");
				$counter++;
			} 
			echo "<br>";
			//echo   strpos( $element->nodeValue, " ") , "<br>";
		}
		else
			break;
    }//foreach
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	$anchors2 = getElementsByClass($dom,'tr','even');
	$i = 0;
	    foreach ($anchors2 as $element) 
    {
		if($element == null || $seprator[1]== null )
			continue;
		if($element->getLineNo() < $seprator[1]->getLineNo() && $element->getLineNo() > $seprator[0]->getLineNo())
		{
			if($element->getElementsByTagName('a')[0] != NULL)
			{
			//	$playerName = rtrim(str_replace("_", " ", rtrim( substr( $element->getElementsByTagName('a')[0]->getAttribute('href'), 12), 
			//		substr( substr( $element->getElementsByTagName('a')[0]->getAttribute('href'), 12) , -10))), "/");
				$playerName = str_replace("_"," ",substr( substr( $element->getElementsByTagName('a')[0]->getAttribute('href'), 12) ,0 , -11));
				
			}
			echo "P:", $playerName;
			$words = explode( ' ', $playerName);
			array_shift( $words);
			$lastName = implode( ' ', $words);
			$firstName = explode(" ", $playerName);
			
			echo "@",$element->nodeValue,"<br>";
			$token = strtok($element->nodeValue, " ");
			////////////////////
			$name = explode(" ", $playerName);
			$counter = 0;
			if(count($name) == 1)
			{
				echo "name is:". $playerName; 
				$sql = "INSERT IGNORE INTO player (teamID, firstName) VALUES ('" .$away. "','". $playerName."')";
				if ($conn->query($sql) === TRUE) {
					echo "New record created successfully";
				}else
					echo "Error: " . $sql . "<br>" . $conn->error;
				
			}
			else if(!( $counter == 0 && $token[0] == 'T' && $token[1] == 'o' && $token[2] = 't'))
			{
				$sql = "INSERT IGNORE INTO player (teamID, firstName, lastName) VALUES ('" .$away. "','". $firstName[0]. "','" . $lastName."')";
			
				if ($conn->query($sql) === TRUE) {
					echo "New record created successfully";
				}else
					echo "Error: " . $sql . "<br>" . $conn->error;
			}
			//if($token == "Total")
				//echo "hereeeeeeeeeeeeeeeeeeeee".$token;
			///////////////
			if(count($name) == 1)
			{
				$sql = "Select playerID FROM player where firstName='".$playerName."'";
			}
			else
				$sql = "Select playerID FROM player where firstName='".$firstName[0]."'and lastName='".$lastName."'";  

			
			$retval = $conn->query($sql);
			//echo $retval;
			while($row = $retval->fetch_assoc())
			{
				echo "playerID :".$row["playerID"]."  <br> ";
				$playerID = $row["playerID"];
			} 	
			$sql = "INSERT IGNORE INTO playerstats (playerID, gameID) VALUES ('" .$playerID. "','". $game."')";
			if ($conn->query($sql) === TRUE) {
				echo "New playerstats created successfully";
			}else
				echo "Error: " . $sql . "<br>" . $conn->error;			

			///////////////
			$total = 0;
			if(strpos( $element->nodeValue,"Total") !== false)
			{
				echo "something";
				$total = 1;
				$element->nodeValue = substr( $element->nodeValue , strpos($element->nodeValue, "240") + 4);
				$sql = "INSERT IGNORE INTO teamstats (teamID, gameID) VALUES ('" .$away. "','". $game."')";
					if ($conn->query($sql) === TRUE) {
						echo "New teamstats created successfully";
					}else
					echo "Error: " . $sql . "<br>" . $conn->error;
			}
			else
			{
				$element->nodeValue = substr( $element->nodeValue , strpos($element->nodeValue, ":")-2);			
				if($element->nodeValue == "" || $element->nodeValue == FALSE )
				{
					echo "here";
					continue;
				}
			}
			$token = strtok($element->nodeValue, " ");
			echo "element is:". $element->nodeValue;
			echo "<br>new token:".$token."---";
			//$t = substr($token,0,1);
			$flag = 0;
			if($total == 1)
			{
				echo "aryan";
				echo "<br>position is:".strpos($element->nodeValue, "290");
				if(strpos($element->nodeValue, "290") != false || strpos($element->nodeValue, "265") || strpos($element->nodeValue, "315")|| strpos($element->nodeValue, "340"))
				{
					$flag = 1;
					echo "yes";
				}
			}
			$counter = 0;
			$DNP = 0;
			if(strpos( $token,"00:00") !== false)
				continue;
			
			///////////////////////////////////
			while ($token !== false)
			{
				if($total == 1)
				{
					if($flag == 1)
					{						
						$token = strtok(" ");
						$token = strtok(" ");
						$token = strtok(" ");
						$flag = 0;
					}
					switch($counter)
					{
		
						case 0:
							$fg = explode('-', $token);
							echo "fg =" . $token;
							echo "fgATTEMP is: ". $fg[1];
							echo "fgMADE is: ". $fg[0];
							$sql = "UPDATE  teamstats SET fgm ='".$fg[0]."', fga ='".$fg[1]."'where teamID='". $away."' AND gameID='".$game."'";
							if ($conn->query($sql) === TRUE) {
								echo "New record created successfully";
							}else
								echo "Error: " . $sql . "<br>" . $conn->error;
							break;
						case 1:
							$fg = explode('-', $token);
							$sql = "UPDATE teamstats SET 3pm ='".$fg[0]."', 3pa ='".$fg[1]."'where teamID='". $away."' AND gameID='".$game."'";
							if ($conn->query($sql) === TRUE) {
								echo "New record created successfully";
							}else
								echo "Error: " . $sql . "<br>" . $conn->error;
							break;		
						case 2:
							$fg = explode('-', $token);
							$sql = "UPDATE teamstats SET ftm ='".$fg[0]."', fta ='".$fg[1]."'where teamID='". $away."' AND gameID='".$game."'";
							if ($conn->query($sql) === TRUE) {
								echo "New record created successfully";
							}else
								echo "Error: " . $sql . "<br>" . $conn->error;
							$token = strtok(" ");
							break;					
	
						case 3:
							$sql = "UPDATE teamstats SET ORB ='".$token."'where teamID='". $away."'AND gameID='".$game."'";
							if ($conn->query($sql) === TRUE) {
								echo "New record created successfully";
							}else
								echo "Error: " . $sql . "<br>" . $conn->error;
							break;	
						case 4:
							$sql = "UPDATE teamstats SET DRB ='".$token."'where teamID='". $away."'AND gameID='".$game."'";
							if ($conn->query($sql) === TRUE) {
								echo "New record created successfully";
							}else
								echo "Error: " . $sql . "<br>" . $conn->error;
							break;	
						case 5:
							$sql = "UPDATE teamstats SET reb ='".$token."'where teamID='". $away."'AND gameID='".$game."'";
							if ($conn->query($sql) === TRUE) {
								echo "New record created successfully";
							}else
								echo "Error: " . $sql . "<br>" . $conn->error;
							break;	
						case 6:
							$sql = "UPDATE teamstats SET ast ='".$token."'where teamID='". $away."'AND gameID='".$game."'";
							if ($conn->query($sql) === TRUE) {
								echo "New record created successfully";
							}else
								echo "Error: " . $sql . "<br>" . $conn->error;
							break;			
						case 7:
							$sql = "UPDATE teamstats SET pf ='".$token."'where teamID='". $away."'AND gameID='".$game."'";
							if ($conn->query($sql) === TRUE) {
								echo "New record created successfully";
							}else
								echo "Error: " . $sql . "<br>" . $conn->error;
							break;	
						case 8:
							$sql = "UPDATE teamstats SET stl ='".$token."'where teamID='". $away."'AND gameID='".$game."'";
							if ($conn->query($sql) === TRUE) {
								echo "New record created successfully";
							}else
								echo "Error: " . $sql . "<br>" . $conn->error;
							break;	
						case 9:
							$sql = "UPDATE teamstats SET tov ='".$token."'where teamID='". $away."'AND gameID='".$game."'";
							if ($conn->query($sql) === TRUE) {
								echo "New record created successfully";
							}else
								echo "Error: " . $sql . "<br>" . $conn->error;
							break;	
						case 10:
							$sql = "UPDATE teamstats SET bs ='".$token."'where teamID='". $away."'AND gameID='".$game."'";
							if ($conn->query($sql) === TRUE) {
								echo "New record created successfully";
							}else
								echo "Error: " . $sql . "<br>" . $conn->error;
							break;			
						case 11:
							break;			
						case 12:
							$sql = "UPDATE teamstats SET pts ='".$token."'where teamID='". $away."'AND gameID='".$game."'";
							if ($conn->query($sql) === TRUE) {
								echo "New record created successfully";
							}else
								echo "Error: " . $sql . "<br>" . $conn->error;
							break;			
					}
				}
				if($total == 1)
				{
					$token = strtok(" ");
					$counter++;
					continue;
				}

				switch($counter)
				{
					case 0:
						$sql = "UPDATE playerstats SET min ='".$token."'where playerID='". $playerID."'AND gameID='".$game."'";
						if ($conn->query($sql) === TRUE) {
							echo "updated succesfully";
						}else
							echo "Error: " . $sql . "<br>" . $conn->error;
						break;
				
					case 1:
						$fg = explode('-', $token);
						echo "fgATTEMP is: ". $fg[1];
						echo "fgMADE is: ". $fg[0];
						$sql = "UPDATE playerstats SET fgm ='".$fg[0]."', fga ='".$fg[1]."'where playerID='". $playerID."'AND gameID='".$game."'";
						if ($conn->query($sql) === TRUE) {
							echo "New record created successfully";
						}else
							echo "Error: " . $sql . "<br>" . $conn->error;
						break;
					case 2:
						$fg = explode('-', $token);
						$sql = "UPDATE playerstats SET 3pm ='".$fg[0]."', 3pa ='".$fg[1]."'where playerID='". $playerID."'AND gameID='".$game."'";
						if ($conn->query($sql) === TRUE) {
							echo "New record created successfully";
						}else
							echo "Error: " . $sql . "<br>" . $conn->error;
						break;		
					case 3:
						$fg = explode('-', $token);
						$sql = "UPDATE playerstats SET ftm ='".$fg[0]."', fta ='".$fg[1]."'where playerID='". $playerID."'AND gameID='".$game."'";
						if ($conn->query($sql) === TRUE) {
							echo "New record created successfully";
						}else
							echo "Error: " . $sql . "<br>" . $conn->error;
						break;					
					case 4:
						$sql = "UPDATE playerstats SET pm ='".$token."'where playerID='". $playerID."'AND gameID='".$game."'";
						if ($conn->query($sql) === TRUE) {
							echo "New record created successfully";
						}else
							echo "Error: " . $sql . "<br>" . $conn->error;
						break;			
					case 5:
						$sql = "UPDATE playerstats SET ORB ='".$token."'where playerID='". $playerID."'AND gameID='".$game."'";
						if ($conn->query($sql) === TRUE) {
							echo "New record created successfully";
						}else
							echo "Error: " . $sql . "<br>" . $conn->error;
						break;	
					case 6:
						$sql = "UPDATE playerstats SET DRB ='".$token."'where playerID='". $playerID."'AND gameID='".$game."'";
						if ($conn->query($sql) === TRUE) {
							echo "New record created successfully";
						}else
							echo "Error: " . $sql . "<br>" . $conn->error;
						break;	
					case 7:
						$sql = "UPDATE playerstats SET reb ='".$token."'where playerID='". $playerID."'AND gameID='".$game."'";
						if ($conn->query($sql) === TRUE) {
							echo "New record created successfully";
						}else
							echo "Error: " . $sql . "<br>" . $conn->error;
						break;	
					case 8:
						$sql = "UPDATE playerstats SET ast ='".$token."'where playerID='". $playerID."'AND gameID='".$game."'";
						if ($conn->query($sql) === TRUE) {
							echo "New record created successfully";
						}else
							echo "Error: " . $sql . "<br>" . $conn->error;
						break;			
					case 9:
						$sql = "UPDATE playerstats SET pf ='".$token."'where playerID='". $playerID."'AND gameID='".$game."'";
						if ($conn->query($sql) === TRUE) {
							echo "New record created successfully";
						}else
							echo "Error: " . $sql . "<br>" . $conn->error;
						break;	
					case 10:
						$sql = "UPDATE playerstats SET stl ='".$token."'where playerID='". $playerID."'AND gameID='".$game."'";
						if ($conn->query($sql) === TRUE) {
							echo "New record created successfully";
						}else
							echo "Error: " . $sql . "<br>" . $conn->error;
						break;	
					case 11:
						$sql = "UPDATE playerstats SET tov ='".$token."'where playerID='". $playerID."'AND gameID='".$game."'";
						if ($conn->query($sql) === TRUE) {
							echo "New record created successfully";
						}else
							echo "Error: " . $sql . "<br>" . $conn->error;
						break;	
					case 12:
						$sql = "UPDATE playerstats SET bs ='".$token."'where playerID='". $playerID."'AND gameID='".$game."'";
						if ($conn->query($sql) === TRUE) {
							echo "New record created successfully";
						}else
							echo "Error: " . $sql . "<br>" . $conn->error;
						break;			
					case 13:
						break;			
					case 14:
						$sql = "UPDATE playerstats SET pts ='".$token."'where playerID='". $playerID."'AND gameID='".$game."'";
						if ($conn->query($sql) === TRUE) {
							echo "New record created successfully";
						}else
							echo "Error: " . $sql . "<br>" . $conn->error;
						break;			
					}
				echo $counter ,$token,"|";
				$token = strtok(" ");
				$counter++;
			} 
			echo "<br>";
		}
		//else
		//	break;
    }//foreach
	/////////////////////////////////////////////////////////////////////////////
	
	echo "Home: <br>"; 
	foreach ($anchors as $element) 
    {
		if($element->getLineNo() > $seprator[1]->getLineNo())
		{
			if($element->getElementsByTagName('a')[0] == null )
				continue;
			$herf = $element->getElementsByTagName('a')[0]->getAttribute('href');
			//echo strpos($herf, "/index.html")."aaa";
			//echo rtrim( $herf, "index.html");
			//echo ltrim(rtrim( $herf, "index.html"),"/playerfile");
			//$playerName = rtrim( str_replace("_", " ", rtrim( substr( $element->getElementsByTagName('a')[0]->getAttribute('href'), 12), 
				//substr( substr( $element->getElementsByTagName('a')[0]->getAttribute('href'), 12) , -10))), "/");
			
			$playerName = str_replace("_"," ",substr( substr( $element->getElementsByTagName('a')[0]->getAttribute('href'), 12) ,0 , -11));
			$words = explode( ' ', $playerName);
			array_shift( $words);
			$lastName = implode( ' ', $words);
			$firstName = explode(" ", $playerName);
			
			
			echo $playerName;
			echo "@", $element->nodeValue,"<br>";
			$token = strtok($element->nodeValue, " ");
			////////////////////
			$name = explode(" ", $playerName);

			$sql = "INSERT IGNORE INTO player (teamID, firstName, lastName) VALUES ('" .$home. "','". $firstName[0]. "','" . $lastName."')";
			if ($conn->query($sql) === TRUE) {
				echo "New record created successfully";
			}else
				echo "Error: " . $sql . "<br>" . $conn->error;
			///////////////			
			$sql = "Select playerID FROM player where firstName='".$firstName[0]."'and lastName='".$lastName."'";  
					
			$retval = $conn->query($sql);
			//echo $retval;
			while($row = $retval->fetch_assoc())
			{
				echo "playerID:".$row["playerID"]."  <br> ";
				$playerID = $row["playerID"];
			} 	
			$sql = "INSERT IGNORE INTO playerstats (playerID, gameID) VALUES ('" .$playerID. "','". $game."')";
			if ($conn->query($sql) === TRUE) {
				echo "New record created successfully";
			}else
				echo "Error: " . $sql . "<br>" . $conn->error;			
			///////////////
			$element->nodeValue = substr( $element->nodeValue , strpos($element->nodeValue, ":")-2);
			if($element->nodeValue == "" || $element->nodeValue == FALSE )
				continue;
			$token = strtok($element->nodeValue, " ");
			
			$counter = 0;
			$DNP = 0;
			if(strpos( $token,"00:00") !== false)
				continue;
			while ($token !== false)
			{
				switch($counter)
				{
					case 0:
						$sql = "UPDATE playerstats SET min ='".$token."'where playerID='". $playerID."'AND gameID='".$game."'";
						if ($conn->query($sql) === TRUE) {
							echo "updated succesfully";
						}else
							echo "Error: " . $sql . "<br>" . $conn->error;
						break;
				
					case 1:
						$fg = explode('-', $token);
						echo "fgATTEMP is: ". $fg[1];
						echo "fgMADE is: ". $fg[0];
						$sql = "UPDATE playerstats SET fgm ='".$fg[0]."', fga ='".$fg[1]."'where playerID='". $playerID."'AND gameID='".$game."'";
						if ($conn->query($sql) === TRUE) {
							echo "New record created successfully";
						}else
							echo "Error: " . $sql . "<br>" . $conn->error;
						break;
					case 2:
						$fg = explode('-', $token);
						$sql = "UPDATE playerstats SET 3pm ='".$fg[0]."', 3pa ='".$fg[1]."'where playerID='". $playerID."'AND gameID='".$game."'";
						if ($conn->query($sql) === TRUE) {
							echo "New record created successfully";
						}else
							echo "Error: " . $sql . "<br>" . $conn->error;
						break;		
					case 3:
						$fg = explode('-', $token);
						$sql = "UPDATE playerstats SET ftm ='".$fg[0]."', fta ='".$fg[1]."'where playerID='". $playerID."'AND gameID='".$game."'";
						if ($conn->query($sql) === TRUE) {
							echo "New record created successfully";
						}else
							echo "Error: " . $sql . "<br>" . $conn->error;
						break;					
					case 4:
						$sql = "UPDATE playerstats SET pm ='".$token."'where playerID='". $playerID."'AND gameID='".$game."'";
						if ($conn->query($sql) === TRUE) {
							echo "New record created successfully";
						}else
							echo "Error: " . $sql . "<br>" . $conn->error;
						break;			
					case 5:
						$sql = "UPDATE playerstats SET ORB ='".$token."'where playerID='". $playerID."'AND gameID='".$game."'";
						if ($conn->query($sql) === TRUE) {
							echo "New record created successfully";
						}else
							echo "Error: " . $sql . "<br>" . $conn->error;
						break;	
					case 6:
						$sql = "UPDATE playerstats SET DRB ='".$token."'where playerID='". $playerID."'AND gameID='".$game."'";
						if ($conn->query($sql) === TRUE) {
							echo "New record created successfully";
						}else
							echo "Error: " . $sql . "<br>" . $conn->error;
						break;	
					case 7:
						$sql = "UPDATE playerstats SET reb ='".$token."'where playerID='". $playerID."'AND gameID='".$game."'";
						if ($conn->query($sql) === TRUE) {
							echo "New record created successfully";
						}else
							echo "Error: " . $sql . "<br>" . $conn->error;
						break;	
					case 8:
						$sql = "UPDATE playerstats SET ast ='".$token."'where playerID='". $playerID."'AND gameID='".$game."'";
						if ($conn->query($sql) === TRUE) {
							echo "New record created successfully";
						}else
							echo "Error: " . $sql . "<br>" . $conn->error;
						break;			
					case 9:
						$sql = "UPDATE playerstats SET pf ='".$token."'where playerID='". $playerID."'AND gameID='".$game."'";
						if ($conn->query($sql) === TRUE) {
							echo "New record created successfully";
						}else
							echo "Error: " . $sql . "<br>" . $conn->error;
						break;	
					case 10:
						$sql = "UPDATE playerstats SET stl ='".$token."'where playerID='". $playerID."'AND gameID='".$game."'";
						if ($conn->query($sql) === TRUE) {
							echo "New record created successfully";
						}else
							echo "Error: " . $sql . "<br>" . $conn->error;
						break;	
					case 11:
						$sql = "UPDATE playerstats SET tov ='".$token."'where playerID='". $playerID."'AND gameID='".$game."'";
						if ($conn->query($sql) === TRUE) {
							echo "New record created successfully";
						}else
							echo "Error: " . $sql . "<br>" . $conn->error;
						break;	
					case 12:
						$sql = "UPDATE playerstats SET bs ='".$token."'where playerID='". $playerID."'AND gameID='".$game."'";
						if ($conn->query($sql) === TRUE) {
							echo "New record created successfully";
						}else
							echo "Error: " . $sql . "<br>" . $conn->error;
						break;			
					case 13:
						break;			
					case 14:
						$sql = "UPDATE playerstats SET pts ='".$token."'where playerID='". $playerID."'AND gameID='".$game."'";
						if ($conn->query($sql) === TRUE) {
							echo "New record created successfully";
						}else
							echo "Error: " . $sql . "<br>" . $conn->error;
						break;			
					}
				echo $counter , "$token";
				$token = strtok(" ");
				$counter++;
			} 
			echo "<br>";
		}
    }//foreach
	////////////////////////////////////////////////////////////////////////////////////
    $done = false;
	foreach ($anchors2 as $element) 
    {
		if($done == true)
			break;
		if($element == null || $seprator[1]== null )
			continue;
		if($element->getLineNo() > $seprator[1]->getLineNo())
		{
			if($element->getElementsByTagName('a')[0] != NULL)
			{
				//$playerName = rtrim( str_replace("_", " ", rtrim( substr( $element->getElementsByTagName('a')[0]->getAttribute('href'), 12), 
					//substr( substr( $element->getElementsByTagName('a')[0]->getAttribute('href'), 12) , -10))), "/");
				$playerName = str_replace("_"," ",substr( substr( $element->getElementsByTagName('a')[0]->getAttribute('href'), 12) ,0 , -11));
				echo $playerName;
			}
			
			$words = explode( ' ', $playerName);
			array_shift( $words);
			$lastName = implode( ' ', $words);
			$firstName = explode(" ", $playerName);
			
			
			
			echo "@",$element->nodeValue,"<br>";
			$token = strtok($element->nodeValue, " ");
			////////////////////
			$counter = 0;
			if( $counter == 0 && $total == 0)
			{	
				echo "in this bitch";
				$name = explode(" ", $playerName);
			
				$sql = "INSERT IGNORE INTO player (teamID, firstName, lastName) VALUES ('" .$away. "','". $firstName[0]. "','" . $lastName."')";
				if ($conn->query($sql) === TRUE) {
					echo "New record created successfully";
				}else
					echo "Error: " . $sql . "<br>" . $conn->error;

				$sql = "Select playerID FROM player where firstName='".$firstName[0]."'and lastName='".$lastName."'";  
				
				$retval = $conn->query($sql);
				//echo $retval;
				while($row = $retval->fetch_assoc())
				{
					echo "playerID  here is:".$row["playerID"]."  <br> ";
					$playerID = $row["playerID"];
				} 	
			}			
			
			$sql = "INSERT IGNORE INTO playerstats (playerID, gameID) VALUES ('" .$playerID. "','". $game."')";
			if ($conn->query($sql) === TRUE) {
				echo "New record created successfully";
			}else
				echo "Error: " . $sql . "<br>" . $conn->error;			
			///////////////
			$total = 0;
			if(strpos( $element->nodeValue,"Total") !== false)
			{
				echo "something";
				$total = 1;
				$element->nodeValue = substr( $element->nodeValue , strpos($element->nodeValue, "240") + 4);
				$sql = "INSERT IGNORE INTO teamstats (teamID, gameID) VALUES ('" .$home. "','". $game."')";
					if ($conn->query($sql) === TRUE) {
						echo "New teamstats created successfully";
					}else
					echo "Error: " . $sql . "<br>" . $conn->error;
			}
			else
			{
				$element->nodeValue = substr( $element->nodeValue , strpos($element->nodeValue, ":")-2);			
				if($element->nodeValue == "" || $element->nodeValue == FALSE )
					continue;
			}
			$token = strtok($element->nodeValue, " ");
			echo "token is:".$token;
			//if(strpos($token, "00:00") != false )
			
			$counter = 0;
			$DNP = 0;
			if(strpos( $token,"00:00") !== false)
				continue;
			if($total == 1)
			{
				echo "aryan";
				echo "<br>position is:".strpos($element->nodeValue, "290");
				if(strpos($element->nodeValue, "290") != false || strpos($element->nodeValue, "265") || strpos($element->nodeValue, "315")|| strpos($element->nodeValue, "340"))
				{
					$flag = 1;
					echo "yes";
				}
			}
			
			///////////////////////////////////
			while ($token !== false)
			{
				if($total == 1)
				{
					if($flag == 1)
						{						
							$token = strtok(" ");
							$token = strtok(" ");
							$token = strtok(" ");
							$flag = 0;
						}
					switch($counter)
					{
	
						case 0:
							echo "counter is:". $counter;
							$fg = explode('-', $token);
							echo "fgATTEMP is: ". $fg[1];
							echo "fgMADE is: ". $fg[0];
							$sql = "UPDATE teamstats SET fgm ='".$fg[0]."', fga ='".$fg[1]."'where teamID='". $home."'AND gameID='".$game."'";
							if ($conn->query($sql) === TRUE) {
								echo "New record created successfully";
							}else
								echo "Error: " . $sql . "<br>" . $conn->error;
							break;
						case 1:
							$fg = explode('-', $token);
							$sql = "UPDATE teamstats SET 3pm ='".$fg[0]."', 3pa ='".$fg[1]."'where teamID='". $home."'AND gameID='".$game."'";
							if ($conn->query($sql) === TRUE) {
								echo "New record created successfully";
							}else
								echo "Error: " . $sql . "<br>" . $conn->error;
							break;		
						case 2:
							$fg = explode('-', $token);
							$sql = "UPDATE teamstats SET ftm ='".$fg[0]."', fta ='".$fg[1]."'where teamID='". $home."'AND gameID='".$game."'";
							if ($conn->query($sql) === TRUE) {
								echo "New record created successfully";
							}else
								echo "Error: " . $sql . "<br>" . $conn->error;
								$token = strtok(" ");
							break;					
						case 3:
							$sql = "UPDATE teamstats SET ORB ='".$token."'where teamID='". $home."'AND gameID='".$game."'";
							if ($conn->query($sql) === TRUE) {
								echo "New record created successfully";
							}else
								echo "Error: " . $sql . "<br>" . $conn->error;
							break;	
						case 4:
							$sql = "UPDATE teamstats SET DRB ='".$token."'where teamID='". $home."'AND gameID='".$game."'";
							if ($conn->query($sql) === TRUE) {
								echo "New record created successfully";
							}else
								echo "Error: " . $sql . "<br>" . $conn->error;
							break;	
						case 5:
							$sql = "UPDATE teamstats SET reb ='".$token."'where teamID='". $home."'AND gameID='".$game."'";
							if ($conn->query($sql) === TRUE) {
								echo "New record created successfully";
							}else
								echo "Error: " . $sql . "<br>" . $conn->error;
							break;	
						case 6:
							$sql = "UPDATE teamstats SET ast ='".$token."'where teamID='". $home."'AND gameID='".$game."'";
							if ($conn->query($sql) === TRUE) {
								echo "New record created successfully";
							}else
								echo "Error: " . $sql . "<br>" . $conn->error;
							break;			
						case 7:
							$sql = "UPDATE teamstats SET pf ='".$token."'where teamID='". $home."'AND gameID='".$game."'";
							if ($conn->query($sql) === TRUE) {
								echo "New record created successfully";
							}else
								echo "Error: " . $sql . "<br>" . $conn->error;
							break;	
						case 8:
							$sql = "UPDATE teamstats SET stl ='".$token."'where teamID='". $home."'AND gameID='".$game."'";
							if ($conn->query($sql) === TRUE) {
								echo "New record created successfully";
							}else
								echo "Error: " . $sql . "<br>" . $conn->error;
							break;	
						case 9:
							$sql = "UPDATE teamstats SET tov ='".$token."'where teamID='". $home."'AND gameID='".$game."'";
							if ($conn->query($sql) === TRUE) {
								echo "New record created successfully";
							}else
								echo "Error: " . $sql . "<br>" . $conn->error;
							break;	
						case 10:
							$sql = "UPDATE teamstats SET bs ='".$token."'where teamID='". $home."'AND gameID='".$game."'";
							if ($conn->query($sql) === TRUE) {
								echo "New record created successfully";
							}else
								echo "Error: " . $sql . "<br>" . $conn->error;
							break;			
						case 11:
							break;			
						case 12:
							$sql = "UPDATE teamstats SET pts ='".$token."'where teamID='". $home."'AND gameID='".$game."'";
							if ($conn->query($sql) === TRUE) {
								echo "New record created successfully";
							}else
								echo "Error: " . $sql . "<br>" . $conn->error;
							$done = true;
							break;			
					}
				}
				if($total == 1)
				{
					echo "im hereeeeeeeeeeeeeeeeeeeeeeeeeeeeee";
					echo "token is:". $token;
					$token = strtok(" ");
					echo "token is:". $token;
					$counter++;
					continue;
				}
				switch($counter)
				{
					case 0:
						$sql = "UPDATE playerstats SET min ='".$token."'where playerID='". $playerID."'AND gameID='".$game."'";
						if ($conn->query($sql) === TRUE) {
							echo "updated succesfully";
						}else
							echo "Error: " . $sql . "<br>" . $conn->error;
						break;
				
					case 1:
						$fg = explode('-', $token);
						echo "fgATTEMP is: ". $fg[1];
						echo "fgMADE is: ". $fg[0];
						$sql = "UPDATE playerstats SET fgm ='".$fg[0]."', fga ='".$fg[1]."'where playerID='". $playerID."'AND gameID='".$game."'";
						if ($conn->query($sql) === TRUE) {
							echo "New record created successfully";
						}else
							echo "Error: " . $sql . "<br>" . $conn->error;
						break;
					case 2:
						$fg = explode('-', $token);
						$sql = "UPDATE playerstats SET 3pm ='".$fg[0]."', 3pa ='".$fg[1]."'where playerID='". $playerID."'AND gameID='".$game."'";
						if ($conn->query($sql) === TRUE) {
							echo "New record created successfully";
						}else
							echo "Error: " . $sql . "<br>" . $conn->error;
						break;		
					case 3:
						$fg = explode('-', $token);
						$sql = "UPDATE playerstats SET ftm ='".$fg[0]."', fta ='".$fg[1]."'where playerID='". $playerID."'AND gameID='".$game."'";
						if ($conn->query($sql) === TRUE) {
							echo "New record created successfully";
						}else
							echo "Error: " . $sql . "<br>" . $conn->error;
						break;					
					case 4:
						$sql = "UPDATE playerstats SET pm ='".$token."'where playerID='". $playerID."'AND gameID='".$game."'";
						if ($conn->query($sql) === TRUE) {
							echo "New record created successfully";
						}else
							echo "Error: " . $sql . "<br>" . $conn->error;
						break;			
					case 5:
						$sql = "UPDATE playerstats SET ORB ='".$token."'where playerID='". $playerID."'AND gameID='".$game."'";
						if ($conn->query($sql) === TRUE) {
							echo "New record created successfully";
						}else
							echo "Error: " . $sql . "<br>" . $conn->error;
						break;	
					case 6:
						$sql = "UPDATE playerstats SET DRB ='".$token."'where playerID='". $playerID."'AND gameID='".$game."'";
						if ($conn->query($sql) === TRUE) {
							echo "New record created successfully";
						}else
							echo "Error: " . $sql . "<br>" . $conn->error;
						break;	
					case 7:
						$sql = "UPDATE playerstats SET reb ='".$token."'where playerID='". $playerID."'AND gameID='".$game."'";
						if ($conn->query($sql) === TRUE) {
							echo "New record created successfully";
						}else
							echo "Error: " . $sql . "<br>" . $conn->error;
						break;	
					case 8:
						$sql = "UPDATE playerstats SET ast ='".$token."'where playerID='". $playerID."'AND gameID='".$game."'";
						if ($conn->query($sql) === TRUE) {
							echo "New record created successfully";
						}else
							echo "Error: " . $sql . "<br>" . $conn->error;
						break;			
					case 9:
						$sql = "UPDATE playerstats SET pf ='".$token."'where playerID='". $playerID."'AND gameID='".$game."'";
						if ($conn->query($sql) === TRUE) {
							echo "New record created successfully";
						}else
							echo "Error: " . $sql . "<br>" . $conn->error;
						break;	
					case 10:
						$sql = "UPDATE playerstats SET stl ='".$token."'where playerID='". $playerID."'AND gameID='".$game."'";
						if ($conn->query($sql) === TRUE) {
							echo "New record created successfully";
						}else
							echo "Error: " . $sql . "<br>" . $conn->error;
						break;	
					case 11:
						$sql = "UPDATE playerstats SET tov ='".$token."'where playerID='". $playerID."'AND gameID='".$game."'";
						if ($conn->query($sql) === TRUE) {
							echo "New record created successfully";
						}else
							echo "Error: " . $sql . "<br>" . $conn->error;
						break;	
					case 12:
						$sql = "UPDATE playerstats SET bs ='".$token."'where playerID='". $playerID."'AND gameID='".$game."'";
						if ($conn->query($sql) === TRUE) {
							echo "New record created successfully";
						}else
							echo "Error: " . $sql . "<br>" . $conn->error;
						break;			
					case 13:
						break;			
					case 14:
						$sql = "UPDATE playerstats SET pts ='".$token."'where playerID='". $playerID."'AND gameID='".$game."'";
						if ($conn->query($sql) === TRUE) {
							echo "New record created successfully";
						}else
							echo "Error: " . $sql . "<br>" . $conn->error;
						break;			
					}
				echo $counter , "$token";
				$token = strtok(" ");
				$counter++;
			} 
			echo "<br>";
			if(  strpos($element->nodeValue , "240") != False)
				break;
		}

    }//foreach
			
}
function getElementsByClass(&$parentNode, $tagName, $className) {
    $nodes=array();

    $childNodeList = $parentNode->getElementsByTagName($tagName);
    for ($i = 0; $i < $childNodeList->length; $i++) {
        $temp = $childNodeList->item($i);
        if (stripos($temp->getAttribute('class'), $className) !== false) {
            $nodes[]=$temp;
			
        }
    }

    return $nodes;
}
$day =28;
$month = 10;
$year = 2014;
echo date("D M d, Y G:i a");
$done =false;

while($month < 17)
{
	if($done == true)
		break;
	while($day < 32)
	{
		if($month == 14 && ( $day == 13 || $day == 15))
			$day++;
		else if($month == 16 && $day == 15)
		{
			$done = true;
			break;
		}
		else if($month < 13)
		{	if($day < 10)
			{
				findGames("2014".$month."0".$day);
				echo "<br>2014".$month."0".$day;
			}
			else
			{
				findGames("2014".$month.$day);
				echo "<br>2014".$month.$day;
			}
		}
		else
		{
			if($month == 14 && ($day == 13 || $day == 15))
			{
				$day = $day + 1;
				continue;
			}
			if($day < 10)
			{
				findGames("20150". ($month-12) ."0" .$day);
				echo "<br>20150".($month-12) . "0" . $day;
			}
			else
			{
				echo "<br>20150".($month-12) .$day;
				findGames("20150". ($month-12) .$day);
			}
		}
		$day = $day + 1;
	}
	$month = $month + 1;
	$day = 1 ;

}
echo date("D M d, Y G:i a");
//findGames("20151030");
//findGames("20151029");