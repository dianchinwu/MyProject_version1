<?php
// $hostname = "localhost";
// $username = "使用者名稱";
// $password = "密碼";
// $dbname = "資料庫名稱";

$hostname = "localhost";
$username = "id7773224_tcnr08";
$password = "123456";
$dbname = "id7773224_friends";

// Create connection
$con = new mysqli($hostname, $username, $password, $dbname);
// Check connection
if ($con->connect_error) {
die("Connection failed: " . $con->connect_error);
}
//----------------------------------------------------------------
$con->query("SET NAMES utf8");
$con->query("SET CHARACTER SET 'UTF8';");
$con->query('SET CHARACTER_SET_CLIENT=UTF8;');
$con->query('SET CHARACTER_SET_RESULTS=UTF8;');
//----------------------------------------------------------------
//$sql = "SELECT * FROM member";
$sql = $_REQUEST['query_string'];
$sql = stripslashes($sql);  //拿掉空格

$result = $con->query($sql);

if ($result->num_rows > 0) {
// output data of each row
while($row = $result->fetch_assoc()) {
    $output[] = $row;
}
} else {
echo "0 results";
}
print(json_encode($output));
$con->close();
?>