import java.sql.*;
import java.util.*;

class Club {
    String name;
    int w=0,d=0,l=0,gf=0,ga=0;

    public Club(String name, int goalSco, int goalConc) {
        this.name = name;
        this.gf += goalSco;
        this.ga += goalConc;
        if(goalSco > goalConc) 
            this.w += 1;
        else if(goalSco < goalConc)
            this.l += 1;
        else
            this.d += 1;
    }
}

class StatsHolder {
    String ref, team;
    int pos=0,p=0,w=0,d=0,l=0,gf=0,ga=0,gd=0,points=0;

    public StatsHolder(int Pos, String Ref, String Team, int P, int W, int D, int L, int GF, int GA, int GD, int POINTS){
        this.pos = Pos;
        this.ref = Ref;
        this.team = Team;
        this.p += P;
        this.w += W;
        this.d += D;
        this.l += L;
        this.gf += GF;
        this.ga += GA;
        this.gd += GD;
        this.points += POINTS; 
    }
}

class Match {
    Club Hteam, Ateam;
    StatsHolder statsHolder, position, tmp;

    Boolean condition = true;

    String driver = "com.mysql.cj.jdbc.Driver";
    String db_url = "jdbc:mysql://localhost:3306/leaguetable";
    String username = "root";
    String password = "";

    public void playedGame() {
        try (Scanner getInfo = new Scanner(System.in)) {
            System.out.print("Home Team: ");
            String hteam = getInfo.nextLine();
            System.out.print("Away Team: ");
            String ateam = getInfo.nextLine();
            System.out.print("Goals Scored(HT): ");
            int gs = getInfo.nextInt();
            System.out.print("Goals Conced(HT): ");
            int gc = getInfo.nextInt();
            Hteam = new Club(hteam.toUpperCase(), gs, gc);
            Ateam = new Club(ateam.toUpperCase(), gc, gs);
        }
    }

    public void updateTeamStats() {
        try {
            Class.forName(driver);
            Connection conn = DriverManager.getConnection(db_url, username, password);
            Statement stmt = conn.createStatement();
            ResultSet rSet = stmt.executeQuery("select * from epl");
            List<String> updates = new ArrayList<>();
            while(rSet.next()) {
                if(rSet.getString("ref").equals(Hteam.name)) {
                    int w = rSet.getInt("w") + Hteam.w;
                    int d = rSet.getInt("d") + Hteam.d;
                    int l = rSet.getInt("l") + Hteam.l;
                    int p = w + d + l;
                    int gf = rSet.getInt("gf") + Hteam.gf;
                    int ga = rSet.getInt("ga") + Hteam.ga;
                    int gd = gf - ga;
                    int points = w * 3 + d;
                    System.out.println("Updating for Home team "+Hteam.name);
                    updates.add("update epl set p = " + p + ", w = " + w + ", d = " + d + ", l = " + l + ", gf = " + gf + ", ga = " + ga + ", gd = " + gd + ", points = " + points + " where ref = '" + Hteam.name +"'");
                } else if(rSet.getString("ref").equals(Ateam.name)) {
                    int w = rSet.getInt("w") + Ateam.w;
                    int d = rSet.getInt("d") + Ateam.d;
                    int l = rSet.getInt("l") + Ateam.l;
                    int p = w + d + l;
                    int gf = rSet.getInt("gf") + Ateam.gf;
                    int ga = rSet.getInt("ga") + Ateam.ga;
                    int gd = gf - ga;
                    int points = w * 3 + d;
                    System.out.println("Updating for Away team "+Ateam.name);
                    updates.add("update epl set p = " + p + ", w = " + w + ", d = " + d + ", l = " + l + ", gf = " + gf + ", ga = " + ga + ", gd = " + gd + ", points = " + points + " where ref = '" + Ateam.name +"'");
                } 
            }

            for (String update : updates) { //update team stats in db
                stmt.executeUpdate(update);
            }

            //reorder table accordingly
            for(int i=0;i<2;i++) {
                if(i==0) {
                    updateTable(Hteam.name);
                } else {
                    condition = true;
                    updateTable(Ateam.name);
                }
            }
            //display table after reorder
            displayTable();
            
            conn.close();
            stmt.close();
        } catch (Exception e) {
            e.getStackTrace();
        }
    }
    
    public void updateTable(String club) {
        try {
            Class.forName(driver);
            Connection conn = DriverManager.getConnection(db_url, username, password);
            Statement stmt = conn.createStatement();
            ResultSet rSet = stmt.executeQuery("select * from epl");

            List<StatsHolder> statslist = new ArrayList<>();
            
            while(rSet.next()) {    //get stats of all the teams and store in an arraylist
                    int pos = rSet.getInt("pos");
                    String ref = rSet.getString("ref");
                    String team = rSet.getString("club");
                    int p = rSet.getInt("p");
                    int w = rSet.getInt("w");
                    int d = rSet.getInt("d");
                    int l = rSet.getInt("l");
                    int gf = rSet.getInt("gf");
                    int ga = rSet.getInt("ga");
                    int gd = rSet.getInt("gd");
                    int points = rSet.getInt("points");

                    statsHolder = new StatsHolder(pos, ref, team, p, w, d, l, gf, ga, gd, points);
                    statslist.add(statsHolder);
            }

            for(StatsHolder stats:statslist){
                if(stats.ref.equals(club)) {
                    position = stats;
                }
            }
            System.out.println("Team: " + position.team + " Pos: " + position.pos);

            while (condition) {     //iterate till the club is well positioned on table
                for(StatsHolder stats:statslist){
                    if(stats.pos == (position.pos-1)) {
                        tmp = stats;
                        if(stats.points < position.points || (stats.points == position.points && stats.gd < position.gd) || (stats.points == position.points && stats.gd == position.gd && stats.gf < position.gf)) {
                            int pos = position.pos;
                            position.pos = tmp.pos;
                            tmp.pos = pos;
                        } else {
                            condition = false;
                        }
                        break;
                    } else if(stats.pos > (position.pos-1)) {
                        condition = false;
                        break;
                    }
                }
                System.out.println("Team: " + position.team + " Pos: " + position.pos);

                List<String> updateTable = new ArrayList<>();
                updateTable.add("update epl set ref='xx',club='zz',p=0,w=0,d=0,l=0,gf=0,ga=0,gd=0,points=0 where pos = "+position.pos);
                updateTable.add("update epl set ref='yy',club='qq',p=0,w=0,d=0,l=0,gf=0,ga=0,gd=0,points=0 where pos = "+tmp.pos);
                updateTable.add("update epl set ref='"+ position.ref+"',club='"+position.team+"',p="+position.p+",w="+position.w+",d="+position.d+",l="+position.l+",gf="+position.gf+",ga="+position.ga+",gd="+position.gd+",points="+position.points+" where pos = "+position.pos);
                updateTable.add("update epl set ref='"+ tmp.ref+"',club='"+tmp.team+"',p="+tmp.p+",w="+tmp.w+",d="+tmp.d+",l="+tmp.l+",gf="+tmp.gf+",ga="+tmp.ga+",gd="+tmp.gd+",points="+tmp.points+" where pos = "+tmp.pos);
                for(String update:updateTable) {
                    stmt.executeUpdate(update);
                }
            }
            
            conn.close();
            stmt.close();
        } catch (Exception e) {
            e.getStackTrace();
        }        
    }

    public void displayTable() {
        try {
            Class.forName(driver);
            Connection conn = DriverManager.getConnection(db_url, username, password);
            Statement stmt = conn.createStatement();
            ResultSet rSet = stmt.executeQuery("select * from epl");

            System.out.println("|-----|-----------------|--------|--------|--------|--------|--------|--------|--------|--------|");
            System.out.printf("| %-3s | %-15s | %-6s | %-6s | %-6s | %-6s | %-6s | %-6s | %-6s | %-6s |\n", "POS", "CLUB", "P","W","D","L","GF","GA","GD","POINTS");
            System.out.println("|-----|-----------------|--------|--------|--------|--------|--------|--------|--------|--------|");
    
            while(rSet.next()) {
                System.out.printf("| %-3d | %-15s | %-6d | %-6d | %-6d | %-6d | %-6d | %-6d | %-6d | %-6d |\n", rSet.getInt("pos"), rSet.getString("club"), rSet.getInt("p"),rSet.getInt("w"),rSet.getInt("d"),rSet.getInt("l"),rSet.getInt("gf"),rSet.getInt("ga"),rSet.getInt("gd"),rSet.getInt("points"));
            System.out.println("|-----|-----------------|--------|--------|--------|--------|--------|--------|--------|--------|");
            }
        } catch (Exception e) {
            e.getStackTrace();
        }
    }
}

public class Tables {
    public static void main(String args[]) {
        Match tst = new Match();
        tst.playedGame();
        tst.updateTeamStats();    
    }
}
