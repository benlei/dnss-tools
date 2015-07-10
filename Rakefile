require "fileutils"
require "inifile"
require "open-uri"
require "tmpdir"

DNSS_INI = "#{ENV['HOME']}/dnss.ini"

$ini = IniFile.load(DNSS_INI)
$static = $ini['common']['output']

desc("Does a full update of the pak files and updates the JSON and pushing to git.")
task :default => :update

desc("Attempts to update the pak to the latest version")
task :update do
  fail "Cannot find version.cfg file: #{$static}/version.cfg" unless File.exists?("#{$static}/version.cfg")

  # get the versions
  version_b = File.readlines("#{$static}/version.cfg")[0]
  version = version_b.strip[8..-1].to_i
  server_version = open($ini["patch"]["version"]) {|f| f.read.strip[8..-1].to_i}

  if version == server_version
    puts "Patch up to date: v#{server_version}"
  else
    tmp = Dir.mktmpdir
    ((version+1)..server_version).each do |v|
      download = $ini["patch"]["download"] % v
      puts "Downloading #{download}"
      filename = download.split("/")[-1]
      open("#{tmp}/#{filename}", "wb") do |pak|
        open(download) {|f| pak << f.read}
      end
    end

    sh "pak", "-s", "-o", "--ini", DNSS_INI, "-O", $static, tmp
    open("#{$static}/version.cfg", "wb") {|cfg| cfg << version_b.gsub(version.to_s, server_version.to_s)}

    Rake::Task["dnt"].reenable
    Rake::Task["dnt"].invoke

    Rake::Task["images"].reenable
    Rake::Task["images"].invoke
  end
end

task :dnt => ["dnt:process", "dnt:collect"]
namespace :dnt do
  task :process do
    sh "processor", DNSS_INI
  end

  task :collect do
    sh "collector", DNSS_INI
  end
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