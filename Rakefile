require "fileutils"
require "inifile"
require "open-uri"
require "tmpdir"

$ini_path = "#{ENV['HOME']}/.dn"
$patch = "#{$ini_path}/patch.ini"
$pak = "#{$ini_path}/pak.ini"
$dnt = "#{$ini_path}/dnt.ini"
$static = "#{ENV['HOME']}/static"

fail "You need a #{$patch} with proper configurations set."  unless File.exists?($patch)
fail "You need a #{$pak} with proper configurations set." unless File.exists?($pak)
fail "You need a #{$dnt} with proper configurations set." unless File.exists?($dnt)
fail "Please git clone your region's static files to #{$static}" unless File.exists?($static)

puts "Loading #{$patch}"
$ini = IniFile.load($patch)

desc("Does a full update of the pak files and updates the JSON and pushing to git.")
task :default => [:update, :dnt, :images]

desc("Attempts to update the pak to the latest version")
task :update do
  fail "Cannot find version.cfg file: #{$static}/version.cfg" unless File.exists?("#{$static}/version.cfg")

  # get the versions
  version = File.readlines("#{$static}/version.config")[0].strip[8..-1].to_i
  server_version = open($ini["patch"]["version"]) {|f| f.read.strip[8..-1].to_i}

  if version == server_version
    puts "Patch up to date: v#{server_version}"
  else
    tmp = Dir.mktmpdir
    ((version+1)..server_version).each do |v|
      download = $ini["patch"]["download"] % v
      filename = download.split("/")[-1]
      open("#{tmp}/#{filename}", "wb") do |pak|
        open(download) {|f| pak << f.read}
      end
    end

    sh "pak", "-s", "--ini", $pak, "-O", $static, tmp
  end
end

task :dnt do
  sh "processor", $dnt
  sh "collector", $dnt
end

task :images do
  dirs = [
    "#{$static}/resource/ui/mainbar",
    "#{$static}/resource/ui/skill"
    ]
    
  dirs.each do |dir|
    Dir.chdir(dir) do
      sh "mogrify -format png *.dds"
      Dir["*.png"].each {|png| sh "pngcrush -ow #{png}"}
    end
  end
end